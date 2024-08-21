package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.*
import org.codeblessing.sourceamazing.schema.api.annotations.*
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.*
import org.codeblessing.sourceamazing.schema.schemacreator.query.ConceptQueryValidator
import org.codeblessing.sourceamazing.schema.schemacreator.query.SchemaQueryValidator
import org.codeblessing.sourceamazing.schema.typemirror.AbstractFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.EnumFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.IntFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactory
import org.codeblessing.sourceamazing.schema.typemirror.ReferenceFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProviderHelper.provideClassMirrors
import kotlin.reflect.KClass

object SchemaCreator {
    private const val SCHEMA_CLASS_DESCRIPTION = "Schema Definition Class"
    private const val CONCEPT_CLASS_DESCRIPTION = "Concept Class"
    private const val FACET_CLASS_DESCRIPTION = "Facet Class"

    @Throws(MalformedSchemaException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: KClass<*>): SchemaImpl {
        return createSchemaFromSchemaClassMirror(MirrorFactory.convertToClassMirror(schemaDefinitionClass))
    }


    @Throws(MalformedSchemaException::class)
    internal fun createSchemaFromSchemaClassMirror(schemaDefinitionClass: ClassMirrorInterface): SchemaImpl {
        validateSchemaClassAnnotations(schemaDefinitionClass)
        SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass)

        val conceptClasses = schemaDefinitionClass.getAnnotationMirror(SchemaAnnotationMirror::class).concepts.provideClassMirrors()
        validateConceptClassesAnnotations(conceptClasses)

        val concepts: MutableMap<ConceptName, ConceptSchema> = mutableMapOf()
        val conceptSimpleNames: MutableSet<String> = mutableSetOf()
        val conceptNames = conceptClasses.map { ConceptName.of(it) }
        conceptClasses.forEach { conceptClass ->
            ConceptQueryValidator.validateAccessorMethodsOfConceptClass(conceptClass)
            val conceptName = ConceptName.of(conceptClass)

            if(conceptSimpleNames.contains(conceptName.simpleName())) {
                throw DuplicateConceptMalformedSchemaException("There is already a concept registered " +
                        "with name '${conceptName.simpleName()}' on schema '${schemaDefinitionClass.longText()}'. " +
                        "Can not register concept class '${conceptClass}'.")
            } else {
                conceptSimpleNames.add(conceptName.simpleName())
            }

            val facetClasses = conceptClass.getAnnotationMirror(ConceptAnnotationMirror::class).facets.provideClassMirrors()
            validateFacetClassesAnnotations(facetClasses)
            val facets: MutableList<FacetSchema> = mutableListOf()
            val facetSimpleNames: MutableSet<String> = mutableSetOf()
            facetClasses.forEach { facetClass ->
                val facetName = FacetName.of(facetClass)
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


    private fun validateSchemaClassAnnotations(schemaDefinitionClass: ClassMirrorInterface) {
        checkIsInterface(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasOnlyAnnotation(listOf(Schema::class), schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
    }

    private fun validateConceptClassesAnnotations(conceptClasses: List<ClassMirrorInterface>) {
        conceptClasses.forEach { conceptClass ->
            checkIsInterface(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasOnlyAnnotation(listOf(Concept::class), conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
        }
    }

    private fun validateFacetClassesAnnotations(facetClasses: Collection<ClassMirrorInterface>) {
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
        facetClass: ClassMirrorInterface,
        conceptNames: List<ConceptName>
    ): FacetSchema {
        val unvalidatedFacetSchema = createUnvalidatedFacetSchema(facetName, facetClass)
        return validatedFacetSchema(unvalidatedFacetSchema, conceptName, conceptNames)
    }

    private class UnvalidatedFacetSchema private constructor(
        val facetName: FacetName,
        val facetType: FacetType,
        val minimumOccurrences: Int,
        val maximumOccurrences: Int,
        val enumerationType: ClassMirrorInterface?,
        val referencedConceptClasses: List<ClassMirrorInterface>,
    ) {
        constructor(facetName: FacetName, facetClass: AbstractFacetAnnotationMirror) : this(
            facetName = facetName,
            facetType = facetClass.facetType,
            minimumOccurrences = facetClass.minimumOccurrences,
            maximumOccurrences = facetClass.maximumOccurrences,
            enumerationType = if(facetClass is EnumFacetAnnotationMirror) facetClass.enumerationClass.provideMirror() else null,
            referencedConceptClasses = if(facetClass is ReferenceFacetAnnotationMirror) facetClass.referencedConcepts.provideClassMirrors() else emptyList(),
        )
    }


    private fun createUnvalidatedFacetSchema(
        facetName: FacetName,
        facetClass: ClassMirrorInterface,
    ): UnvalidatedFacetSchema {
        val possibleFacetClasses: Map<KClass<out Annotation>, KClass<out AbstractFacetAnnotationMirror>> = mapOf(
            StringFacet::class to StringFacetAnnotationMirror::class,
            BooleanFacet::class to BooleanFacetAnnotationMirror::class,
            IntFacet::class to IntFacetAnnotationMirror::class,
            EnumFacet::class to EnumFacetAnnotationMirror::class,
            ReferenceFacet::class to ReferenceFacetAnnotationMirror::class,
        )
        return possibleFacetClasses
            .filter { facetAnnotation ->
                facetClass.hasAnnotation(facetAnnotation.key)
            }
            .map { facetAnnotation ->
                UnvalidatedFacetSchema(facetName, facetClass.getAnnotationMirror(facetAnnotation.value))
            }
            .firstOrNull()
            ?: throw IllegalStateException("No supported facet type on $facetClass.")
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
            .map { ConceptName.of(it) }.toSet()


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

    private fun checkIsInterface(classToInspect: ClassMirrorInterface, classDescription: String) {
        if(!classToInspect.isInterface || classToInspect.isAnnotation) {
            throw NotInterfaceMalformedSchemaException("$classDescription '${classToInspect.longText()}' must be an interface.")
        }
    }

    private fun checkHasOnlyAnnotation(permittedAnnotations: List<KClass<out Annotation>>, classToInspect: ClassMirrorInterface, classDescription: String) {
        classToInspect.annotations
            .filter { it.isAnnotationFromSourceAmazing() }
            .forEach { annotationOnClass ->
                if(!permittedAnnotations.any { annotationOnClass.isAnnotation(it) }) {
                    throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' can not have annotation of type '${annotationOnClass.annotationClass.longText()}'.")
                }
            }
    }

    private fun checkHasAnnotation(annotation: KClass<out Annotation>, classToInspect: ClassMirrorInterface, classDescription: String, maxRepeatable: Int = 1) {
        if(!classToInspect.hasAnnotation(annotation)) {
            throw MissingAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' must have an annotation of type '${annotation.annotationText()}'.")
        }

        if(classToInspect.numberOfAnnotation(annotation) > maxRepeatable) {
            throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' can not have more than $maxRepeatable annotation of type '${annotation.annotationText()}'.")
        }
    }

    private fun checkHasExactlyOneOfAnnotation(annotations: List<KClass<out Annotation>>, classToInspect: ClassMirrorInterface, classDescription: String) {
        val numberOfAnnotations = annotations.count { annotation -> hasClassAnnotation(annotation, classToInspect) }

        if(numberOfAnnotations < 1) {
            throw MissingAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' must have one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        } else if(numberOfAnnotations > 1) {
            throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' can not have more than one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        }
    }

    private fun checkHasNotAnnotation(annotation: KClass<out Annotation>, classToInspect: ClassMirrorInterface, classDescription: String) {
        if(hasClassAnnotation(annotation, classToInspect)) {
            throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.longText()}' must not have an annotation of type '${annotation.annotationText()}'.")
        }
    }

    private fun hasClassAnnotation(annotation: KClass<out Annotation>, classToInspect: ClassMirrorInterface): Boolean {
        return classToInspect.hasAnnotation(annotation)
    }
}
