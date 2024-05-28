package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.*
import org.codeblessing.sourceamazing.schema.api.annotations.*
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.*
import org.codeblessing.sourceamazing.schema.schemacreator.query.ConceptQueryValidator
import org.codeblessing.sourceamazing.schema.schemacreator.query.SchemaQueryValidator
import org.codeblessing.sourceamazing.schema.util.AnnotationUtil
import kotlin.reflect.KClass

object SchemaCreator {
    private const val SCHEMA_CLASS_DESCRIPTION = "Schema Definition Class"
    private const val CONCEPT_CLASS_DESCRIPTION = "Concept Class"
    private const val FACET_CLASS_DESCRIPTION = "Facet Class"

    @Throws(MalformedSchemaException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: KClass<*>): SchemaImpl {
        validateSchemaClassAnnotations(schemaDefinitionClass)
        SchemaQueryValidator.validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass)

        val conceptClasses = AnnotationUtil.getAnnotation(schemaDefinitionClass, Schema::class).concepts
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

            val facetClasses = AnnotationUtil.getAnnotation(conceptClass, Concept::class).facets
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

    private fun validateSchemaClassAnnotations(schemaDefinitionClass: KClass<*>) {
        checkIsInterface(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNotAnnotation(Concept::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNotAnnotation(StringFacet::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNotAnnotation(BooleanFacet::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNotAnnotation(IntFacet::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNotAnnotation(EnumFacet::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNotAnnotation(ReferenceFacet::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
    }

    private fun validateConceptClassesAnnotations(conceptClasses: Array<KClass<*>>) {
        conceptClasses.forEach { conceptClass ->
            checkIsInterface(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(Schema::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(StringFacet::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(BooleanFacet::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(IntFacet::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(EnumFacet::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(ReferenceFacet::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
        }
    }

    private fun validateFacetClassesAnnotations(facetClasses: Array<KClass<*>>) {
        facetClasses.forEach { facetClass ->
            checkIsInterface(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasExactlyOneOfAnnotation(
                StringFacet::class,
                BooleanFacet::class,
                IntFacet::class,
                EnumFacet::class,
                ReferenceFacet::class,
                classToInspect = facetClass,
                classDescription = FACET_CLASS_DESCRIPTION)
            checkHasNotAnnotation(Schema::class, facetClass, FACET_CLASS_DESCRIPTION)
            checkHasNotAnnotation(Concept::class, facetClass, FACET_CLASS_DESCRIPTION)
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

    private class UnvalidatedFacetSchema private constructor(
        val facetName: FacetName,
        val facetType: FacetType,
        val minimumOccurrences: Int,
        val maximumOccurrences: Int,
        val enumerationType: KClass<*>? = null,
        val referencedConceptClasses: Array<KClass<*>> = emptyArray(),
    ) {
        constructor(facetName: FacetName, facetClass: StringFacet) : this(
            facetName = facetName,
            facetType = FacetType.TEXT,
            minimumOccurrences = facetClass.minimumOccurrences,
            maximumOccurrences = facetClass.maximumOccurrences,
        )
        constructor(facetName: FacetName, facetClass: BooleanFacet) : this(
            facetName = facetName,
            facetType = FacetType.BOOLEAN,
            minimumOccurrences = facetClass.minimumOccurrences,
            maximumOccurrences = facetClass.maximumOccurrences,
        )
        constructor(facetName: FacetName, facetClass: IntFacet) : this(
            facetName = facetName,
            facetType = FacetType.NUMBER,
            minimumOccurrences = facetClass.minimumOccurrences,
            maximumOccurrences = facetClass.maximumOccurrences,
        )
        constructor(facetName: FacetName, facetClass: EnumFacet) : this(
            facetName = facetName,
            facetType = FacetType.TEXT_ENUMERATION,
            minimumOccurrences = facetClass.minimumOccurrences,
            maximumOccurrences = facetClass.maximumOccurrences,
            enumerationType = facetClass.enumerationClass,
        )
        constructor(facetName: FacetName, facetClass: ReferenceFacet) : this(
            facetName = facetName,
            facetType = FacetType.REFERENCE,
            minimumOccurrences = facetClass.minimumOccurrences,
            maximumOccurrences = facetClass.maximumOccurrences,
            referencedConceptClasses = facetClass.referencedConcepts,
        )
    }


    private fun createUnvalidatedFacetSchema(
        facetName: FacetName,
        facetClass: KClass<*>,
    ): UnvalidatedFacetSchema {
        return if(AnnotationUtil.hasAnnotation(facetClass, StringFacet::class)) {
            UnvalidatedFacetSchema(facetName, AnnotationUtil.getAnnotation(facetClass, StringFacet::class))
        } else if(AnnotationUtil.hasAnnotation(facetClass, BooleanFacet::class)) {
            UnvalidatedFacetSchema(facetName, AnnotationUtil.getAnnotation(facetClass, BooleanFacet::class))
        } else if(AnnotationUtil.hasAnnotation(facetClass, IntFacet::class)) {
            UnvalidatedFacetSchema(facetName, AnnotationUtil.getAnnotation(facetClass, IntFacet::class))
        } else if(AnnotationUtil.hasAnnotation(facetClass, EnumFacet::class)) {
            UnvalidatedFacetSchema(facetName, AnnotationUtil.getAnnotation(facetClass, EnumFacet::class))
        } else if(AnnotationUtil.hasAnnotation(facetClass, ReferenceFacet::class)) {
            UnvalidatedFacetSchema(facetName, AnnotationUtil.getAnnotation(facetClass, ReferenceFacet::class))
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
        val enumerationType =  facetSchema.enumerationType ?: Unit::class
        val referencedConcepts =  facetSchema.referencedConceptClasses
            .map { ConceptName.of(it) }.toSet()


        if(facetType == FacetType.TEXT_ENUMERATION && !enumerationType.java.isEnum) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                "is declared as type '${FacetType.TEXT_ENUMERATION}' but the enumeration is not " +
                "defined or not a real enumeration class (was '$enumerationType')."
            )
        }

        if(facetType != FacetType.TEXT_ENUMERATION && enumerationType != Unit::class) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                "has declared an enumeration class '$enumerationType' but the type of the facet " +
                "is '$facetType' instead of '${FacetType.TEXT_ENUMERATION}'."
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

        return org.codeblessing.sourceamazing.schema.schemacreator.FacetSchemaImpl(
            facetName = facetName,
            facetType = facetType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
            referencingConcepts = referencedConcepts,
            enumerationType = enumerationType,
        )
    }

    private fun checkIsInterface(classToInspect: KClass<*>, classDescription: String) {
        if(!classToInspect.java.isInterface) {
            throw NotInterfaceMalformedSchemaException("$classDescription '${classToInspect.java.longText()}' must be an interface.")
        }
    }

    private fun checkHasAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        if(!hasClassAnnotation(annotation, classToInspect)) {
            throw MissingAnnotationMalformedSchemaException("$classDescription '${classToInspect.java.longText()}' must have an annotation of type '${annotation.annotationText()}'.")
        }
    }

    private fun checkHasExactlyOneOfAnnotation(vararg annotations: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        val numberOfAnnotations = annotations.count { annotation -> hasClassAnnotation(annotation, classToInspect) }

        if(numberOfAnnotations < 1) {
            throw MissingAnnotationMalformedSchemaException("$classDescription '${classToInspect.java.longText()}' must have one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        } else if(numberOfAnnotations > 1) {
            throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.java.longText()}' can not have more than one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        }
    }

    private fun checkHasNotAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        if(hasClassAnnotation(annotation, classToInspect)) {
            throw WrongAnnotationMalformedSchemaException("$classDescription '${classToInspect.java.longText()}' must not have an annotation of type '${annotation.annotationText()}'.")
        }
    }

    private fun hasClassAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>): Boolean {
        return classToInspect.java.getAnnotation(annotation.java) != null
    }
}
