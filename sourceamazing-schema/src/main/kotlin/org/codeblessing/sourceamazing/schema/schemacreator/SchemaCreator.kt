package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.ConceptSchema
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateConceptMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateFacetMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalityMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongTypeMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.query.ConceptQueryValidator
import org.codeblessing.sourceamazing.schema.schemacreator.query.SchemaQueryValidator
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.getAnnotation
import org.codeblessing.sourceamazing.schema.type.getNumberOfAnnotation
import org.codeblessing.sourceamazing.schema.type.hasAnnotation
import org.codeblessing.sourceamazing.schema.type.isAnnotation
import org.codeblessing.sourceamazing.schema.type.isAnnotationFromSourceAmazing
import org.codeblessing.sourceamazing.schema.type.isEnum
import org.codeblessing.sourceamazing.schema.type.isInterface
import kotlin.reflect.KClass

object SchemaCreator {
    private const val SCHEMA_CLASS_DESCRIPTION = "Schema Definition Class"
    private const val CONCEPT_CLASS_DESCRIPTION = "Concept Class"
    private const val FACET_CLASS_DESCRIPTION = "Facet Class"

    @Throws(MalformedSchemaException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: KClass<*>): SchemaImpl {
        validateSchemaClassAnnotations(schemaDefinitionClass)
        SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass)

        val conceptClasses = schemaDefinitionClass.getAnnotation<Schema>().concepts.toList()
        validateConceptClassesAnnotations(conceptClasses)

        val concepts: MutableMap<ConceptName, ConceptSchema> = mutableMapOf()
        val conceptSimpleNames: MutableSet<String> = mutableSetOf()
        val conceptNames = conceptClasses.map { it.toConceptName() }
        conceptClasses.forEach { conceptClass ->
            ConceptQueryValidator.validateAccessorMethodsOfConceptClass(conceptClass)
            val conceptName = conceptClass.toConceptName()

            if(conceptSimpleNames.contains(conceptName.simpleName())) {
                throw DuplicateConceptMalformedSchemaException("There is already a concept registered " +
                        "with name '${conceptName.simpleName()}' on schema '${schemaDefinitionClass.longText()}'. " +
                        "Can not register concept class '${conceptClass}'.")
            } else {
                conceptSimpleNames.add(conceptName.simpleName())
            }

            val facetClasses = conceptClass.getAnnotation<Concept>().facets.toList()
            validateFacetClassesAnnotations(facetClasses)
            val facets: MutableList<FacetSchema> = mutableListOf()
            val facetSimpleNames: MutableSet<String> = mutableSetOf()
            facetClasses.forEach { facetClass ->
                val facetName = facetClass.toFacetName()
                if(facetSimpleNames.contains(facetName.simpleName())) {
                    throw DuplicateFacetMalformedSchemaException("There is already a facet registered " +
                            "with name '${facetName.simpleName()}' on concept '$conceptName'. " +
                            "Can not register facet class '${facetClass}'.")
                } else {
                    facetSimpleNames.add(facetName.simpleName())
                }
                facets += createFacetSchema(conceptName, facetName, facetClass, conceptNames)
            }
            val conceptSchema = ConceptSchemaImpl(
                conceptName = conceptName,
                facets = facets
            )
            concepts[conceptName] = conceptSchema

        }

        return SchemaImpl(concepts)
    }


    private fun validateSchemaClassAnnotations(schemaDefinitionClass: KClass<*>) {
        checkIsInterface(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasOnlyAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
    }

    private fun validateConceptClassesAnnotations(conceptClasses: List<KClass<*>>) {
        conceptClasses.forEach { conceptClass ->
            checkIsInterface(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasOnlyAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
        }
    }

    private fun validateFacetClassesAnnotations(facetClasses: Collection<KClass<*>>) {
        facetClasses.forEach { facetClass ->
            checkIsInterface(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasExactlyOneOfAnnotation(
                annotations = listOf(
                    StringFacet::class,
                    BooleanFacet::class,
                    IntFacet::class,
                    EnumFacet::class,
                    ReferenceFacet::class,
                ),
                classToInspect = facetClass,
                classDescription = FACET_CLASS_DESCRIPTION)

            checkHasOnlyAnnotation(listOf(
                StringFacet::class,
                BooleanFacet::class,
                IntFacet::class,
                EnumFacet::class,
                ReferenceFacet::class,
            ), facetClass, FACET_CLASS_DESCRIPTION)
        }
    }

    private fun createFacetSchema(
        conceptName: ConceptName,
        facetName: FacetName,
        facetClass: KClass<*>,
        conceptNames: List<ConceptName>
    ): FacetSchema {
        val unvalidatedFacetSchema = createUnvalidatedFacetSchema(facetName, facetClass)
        return validatedFacetSchema(unvalidatedFacetSchema, conceptName, conceptNames)
    }

    private class UnvalidatedFacetSchema (
        val facetName: FacetName,
        val facetType: FacetType,
        val minimumOccurrences: Int,
        val maximumOccurrences: Int,
        val enumerationType: KClass<*>?,
        val referencedConceptClasses: List<KClass<*>>,
    )

    private fun createUnvalidatedFacetSchema(
        facetName: FacetName,
        facetClass: KClass<*>,
    ): UnvalidatedFacetSchema {
        return if(facetClass.hasAnnotation(StringFacet::class)) {
            val stringFacet = facetClass.getAnnotation(StringFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.TEXT,
                minimumOccurrences = stringFacet.minimumOccurrences,
                maximumOccurrences = stringFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

            )
        } else if(facetClass.hasAnnotation(BooleanFacet::class)) {
            val booleanFacet = facetClass.getAnnotation(BooleanFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.BOOLEAN,
                minimumOccurrences = booleanFacet.minimumOccurrences,
                maximumOccurrences = booleanFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotation(IntFacet::class)) {
            val intFacet = facetClass.getAnnotation(IntFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.NUMBER,
                minimumOccurrences = intFacet.minimumOccurrences,
                maximumOccurrences = intFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotation(EnumFacet::class)) {
            val enumFacet = facetClass.getAnnotation(EnumFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.TEXT_ENUMERATION,
                minimumOccurrences = enumFacet.minimumOccurrences,
                maximumOccurrences = enumFacet.maximumOccurrences,
                enumerationType = enumFacet.enumerationClass,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotation(ReferenceFacet::class)) {
            val referenceFacet = facetClass.getAnnotation(ReferenceFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.REFERENCE,
                minimumOccurrences = referenceFacet.minimumOccurrences,
                maximumOccurrences = referenceFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = referenceFacet.referencedConcepts.toList(),

                )
        } else {
            throw IllegalStateException("No supported facet type on $facetClass.")
        }
    }

    private fun validatedFacetSchema(
        facetSchema: UnvalidatedFacetSchema,
        conceptName: ConceptName,
        conceptNames: List<ConceptName>
    ): FacetSchema {
        val facetName: FacetName = facetSchema.facetName

        val facetType = facetSchema.facetType
        val minimumOccurrences = facetSchema.minimumOccurrences
        val maximumOccurrences = facetSchema.maximumOccurrences
        val enumerationType =  facetSchema.enumerationType
        val referencedConcepts =  facetSchema.referencedConceptClasses
            .map { it.toConceptName() }.toSet()


        if(facetType == FacetType.TEXT_ENUMERATION && (enumerationType == null || !enumerationType.isEnum)) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                        "is declared as type '${FacetType.TEXT_ENUMERATION}' but the enumeration is not " +
                        "defined or not a real enumeration class (was '$enumerationType')."
            )
        }


        if(facetType == FacetType.REFERENCE && referencedConcepts.isEmpty()) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                "is declared as type '${FacetType.REFERENCE}' " +
                "but the list of concept type is empty."
            )
        }

        if(facetType != FacetType.REFERENCE && referencedConcepts.isNotEmpty()) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                "is not declared as type '${FacetType.REFERENCE}' (is '${facetType}') but the list " +
                "of concept type is not empty (is '${referencedConcepts}')."
            )
        }

        if(minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongCardinalityMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                "has negative cardinalities. Only number greater/equal zero are allowed, " +
                "but was $minimumOccurrences/$maximumOccurrences."
            )
        }

        if(minimumOccurrences > maximumOccurrences) {
            throw WrongCardinalityMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                "has a greater minimumOccurrences ($minimumOccurrences) " +
                "than the maximumOccurrences ($maximumOccurrences)."
            )
        }

        referencedConcepts.forEach { referencedConcept ->
            if(!conceptNames.contains(referencedConcept)) {
                throw WrongTypeMalformedSchemaException(
                    "Facet '$facetName' on concept '$conceptName' " +
                    "has an reference concept '${referencedConcept}' which is not a known concept " +
                    "(known concepts are '${conceptNames}')."
                )
            }
        }

        return FacetSchemaImpl(
            facetName = facetName,
            facetType = facetType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
            referencingConcepts = referencedConcepts,
            enumerationType = enumerationType,
        )
    }

    private fun checkIsInterface(classToInspect: KClass<*>, classDescription: String) {
        if(!classToInspect.isInterface || classToInspect.isAnnotation) {
            throw NotInterfaceMalformedSchemaException("$classDescription '${classToInspect.longText()}' must be an interface.")
        }
    }

    private fun checkHasOnlyAnnotation(permittedAnnotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        return checkHasOnlyAnnotation(listOf(permittedAnnotation), classToInspect, classDescription)
    }

    private fun checkHasOnlyAnnotation(permittedAnnotations: List<KClass<out Annotation>>, classToInspect: KClass<*>, classDescription: String) {
        classToInspect.annotations
            .filter { it.isAnnotationFromSourceAmazing() }
            .forEach { annotationOnClass ->
                if(!permittedAnnotations.contains(annotationOnClass.annotationClass)) {
                    throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' can not have annotation of type '${annotationOnClass.annotationClass.longText()}'.")
                }
            }
    }

    private fun checkHasAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String, maxRepeatable: Int = 1) {
        if(!classToInspect.hasAnnotation(annotation)) {
            throw MissingAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' must have an annotation of type '${annotation.annotationText()}'.")
        }

        if(classToInspect.getNumberOfAnnotation(annotation) > maxRepeatable) {
            throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' can not have more than $maxRepeatable annotation of type '${annotation.annotationText()}'.")
        }
    }

    private fun checkHasExactlyOneOfAnnotation(annotations: List<KClass<out Annotation>>, classToInspect: KClass<*>, classDescription: String) {
        val numberOfAnnotations = annotations.count { annotation -> hasClassAnnotation(annotation, classToInspect) }

        if(numberOfAnnotations < 1) {
            throw MissingAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' must have one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        } else if(numberOfAnnotations > 1) {
            throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' can not have more than one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        }
    }

    private fun hasClassAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>): Boolean {
        return classToInspect.hasAnnotation(annotation)
    }
}
