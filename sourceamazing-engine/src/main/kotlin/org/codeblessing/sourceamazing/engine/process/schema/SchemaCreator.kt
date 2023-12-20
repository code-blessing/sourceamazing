package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.FacetSchema
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.*
import org.codeblessing.sourceamazing.engine.process.schema.query.ConceptQueryValidator
import org.codeblessing.sourceamazing.engine.process.schema.query.SchemaQueryValidator
import org.codeblessing.sourceamazing.engine.process.util.AnnotationUtil
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
        val conceptNames = conceptClasses.map { ConceptName.of(it) }
        conceptClasses.forEach { conceptClass ->
            ConceptQueryValidator.validateAccessorMethodsOfConceptClass(conceptClass)
            val conceptName = ConceptName.of(conceptClass)

            if(concepts.containsKey(conceptName)) {
                throw DuplicateConceptMalformedSchemaException("Concept '$conceptName' is already registered on schema '${schemaDefinitionClass.longText()}'.")
            }

            val facetClasses = AnnotationUtil.getAnnotation(conceptClass, Concept::class).facets
            validateFacetClassesAnnotations(facetClasses)
            val facets: MutableList<FacetSchema> = mutableListOf()
            facetClasses.forEach { facetClass ->
                val facetName = FacetName.of(facetClass)
                if(facets.map { it.facetName }.contains(facetName)) {
                    throw DuplicateFacetMalformedSchemaException("Facet '$facetName' is already registered for concept '$conceptName'.")
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
        checkHasNotAnnotation(Facet::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
    }

    private fun validateConceptClassesAnnotations(conceptClasses: Array<KClass<*>>) {
        conceptClasses.forEach { conceptClass ->
            checkIsInterface(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(Schema::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNotAnnotation(Facet::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
        }
    }

    private fun validateFacetClassesAnnotations(facetClasses: Array<KClass<*>>) {
        facetClasses.forEach { facetClass ->
            checkIsInterface(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasAnnotation(Facet::class, facetClass, FACET_CLASS_DESCRIPTION)
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
        val facetType = AnnotationUtil.getAnnotation(facetClass, Facet::class).type
        val minimumOccurrences = AnnotationUtil.getAnnotation(facetClass, Facet::class).minimumOccurrences
        val maximumOccurrences = AnnotationUtil.getAnnotation(facetClass, Facet::class).maximumOccurrences
        val enumerationType =  AnnotationUtil.getAnnotation(facetClass, Facet::class).enumerationClass
        val referencedConcepts =  AnnotationUtil.getAnnotation(facetClass, Facet::class).referencedConcepts
            .map { ConceptName.of(it) }.toSet()

        if(facetType == FacetType.TEXT_ENUMERATION && !enumerationType.java.isEnum) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                "is declared as type '${FacetType.TEXT_ENUMERATION}' but the enumeration is not " +
                "defined or not a real enumeration class (was '$enumerationType').")
        }

        if(facetType != FacetType.TEXT_ENUMERATION && enumerationType != Unit::class) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                    "has declared an enumeration class '$enumerationType' but the type of the facet " +
                    "is '$facetType' instead of '${FacetType.TEXT_ENUMERATION}'.")
        }

        if(facetType == FacetType.REFERENCE && referencedConcepts.isEmpty()) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                    "is declared as type '${FacetType.REFERENCE}' but the list of concept type is empty.")
        }

        if(facetType != FacetType.REFERENCE && referencedConcepts.isNotEmpty()) {
            throw WrongTypeMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                    "is not declared as type '${FacetType.REFERENCE}' (is '${facetType}') but the list " +
                    "of concept type is not empty (is '${referencedConcepts}').")
        }

        if(minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongCardinalityMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                    "has negative cardinalities. Only number greater/equal zero are allowed, " +
                    "but was $minimumOccurrences/$maximumOccurrences.")
        }

        if(minimumOccurrences > maximumOccurrences) {
            throw WrongCardinalityMalformedSchemaException(
                "Facet '$facetName' on concept '$conceptName' " +
                    "has a greater minimumOccurrences ($minimumOccurrences) " +
                    "than the maximumOccurrences ($maximumOccurrences).")
        }

        referencedConcepts.forEach { referencedConcept ->
            if(!conceptNames.contains(referencedConcept)) {
                throw WrongTypeMalformedSchemaException(
                    "Facet '$facetName' on concept '$conceptName' " +
                        "has an reference concept '${referencedConcept}' which is not a known concept " +
                        "(known concepts are '${conceptNames}').")

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
        if(!classToInspect.java.isInterface) {
            throw NotInterfaceMalformedSchemaException("$classDescription '${classToInspect.java.longText()}' must be an interface.")
        }
    }

    private fun checkHasAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        if(!hasClassAnnotation(annotation, classToInspect)) {
            throw MissingAnnotationMalformedSchemaException("$classDescription '${classToInspect.java.longText()}' must have an annotation of type '${annotation.annotationText()}'.")
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
