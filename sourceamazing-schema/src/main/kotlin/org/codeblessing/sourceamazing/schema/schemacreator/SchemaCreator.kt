package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.ConceptSchema
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateConceptSchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateFacetSchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalitySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.query.QueryMethodsValidator
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasAnnotation
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasExactNumberOfAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasExactlyOneOfAnnotation
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoExtensionFunctions
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoGenericTypeParameters
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoMembers
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoProperties
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasOnlyAnnotation
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasOnlyAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkIsOrdinaryInterface
import org.codeblessing.sourceamazing.schema.type.getAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.schema.type.hasAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.schema.type.isEnum
import kotlin.reflect.KClass

object SchemaCreator {
    private const val SCHEMA_CLASS_DESCRIPTION = "Schema Definition Class"
    private const val CONCEPT_CLASS_DESCRIPTION = "Concept Class"
    private const val FACET_CLASS_DESCRIPTION = "Facet Class"

    @Throws(SyntaxException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: KClass<*>): SchemaImpl {
        validateSchemaClass(schemaDefinitionClass)
        QueryMethodsValidator.validateQueryMethodsOfSchema(schemaDefinitionClass)

        val conceptClasses = schemaDefinitionClass.getAnnotationIncludingSuperclasses<Schema>().concepts.toList()
        validateConceptClasses(conceptClasses)

        val concepts: MutableMap<ConceptName, ConceptSchema> = mutableMapOf()
        val conceptSimpleNames: MutableSet<String> = mutableSetOf()
        val conceptNames = conceptClasses.map { it.toConceptName() }
        conceptClasses.forEach { conceptClass ->
            QueryMethodsValidator.validateQueryMethodsOfConcept(conceptClass)
            val conceptName = conceptClass.toConceptName()

            if(conceptSimpleNames.contains(conceptName.simpleName())) {
                throw DuplicateConceptSchemaSyntaxException(SchemaErrorCode.DUPLICATE_CONCEPTS_ON_SCHEMA, conceptName.simpleName(), schemaDefinitionClass.longText(), conceptClass)
            } else {
                conceptSimpleNames.add(conceptName.simpleName())
            }

            val facetClasses = conceptClass.getAnnotationIncludingSuperclasses<Concept>().facets.toList()
            validateFacetClasses(facetClasses)
            val facets: MutableList<FacetSchema> = mutableListOf()
            val facetSimpleNames: MutableSet<String> = mutableSetOf()
            facetClasses.forEach { facetClass ->
                val facetName = facetClass.toFacetName()
                if(facetSimpleNames.contains(facetName.simpleName())) {
                    throw DuplicateFacetSchemaSyntaxException(SchemaErrorCode.DUPLICATE_FACET_ON_CONCEPT, facetName.simpleName(), conceptName, facetClass.longText())
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


    private fun validateSchemaClass(schemaDefinitionClass: KClass<*>) {
        checkIsOrdinaryInterface(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNoGenericTypeParameters(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNoExtensionFunctions(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNoProperties(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasExactNumberOfAnnotations(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION, numberOf = 1)
        checkHasOnlyAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
    }

    private fun validateConceptClasses(conceptClasses: List<KClass<*>>) {
        conceptClasses.forEach { conceptClass ->
            checkIsOrdinaryInterface(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNoGenericTypeParameters(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNoExtensionFunctions(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNoProperties(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasExactNumberOfAnnotations(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION, numberOf = 1)
            checkHasOnlyAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
        }
    }

    private fun validateFacetClasses(facetClasses: Collection<KClass<*>>) {
        facetClasses.forEach { facetClass ->
            checkIsOrdinaryInterface(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasNoGenericTypeParameters(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasNoExtensionFunctions(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasNoProperties(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasNoMembers(facetClass, FACET_CLASS_DESCRIPTION)

            checkHasExactlyOneOfAnnotation(
                annotations = listOf(
                    StringFacet::class,
                    BooleanFacet::class,
                    IntFacet::class,
                    EnumFacet::class,
                    ReferenceFacet::class,
                ),
                classToInspect = facetClass,
                classDescription = FACET_CLASS_DESCRIPTION
            )
            checkHasOnlyAnnotations(listOf(
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
        return if(facetClass.hasAnnotationIncludingSuperclasses(StringFacet::class)) {
            val stringFacet = facetClass.getAnnotationIncludingSuperclasses(StringFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.TEXT,
                minimumOccurrences = stringFacet.minimumOccurrences,
                maximumOccurrences = stringFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

            )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(BooleanFacet::class)) {
            val booleanFacet = facetClass.getAnnotationIncludingSuperclasses(BooleanFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.BOOLEAN,
                minimumOccurrences = booleanFacet.minimumOccurrences,
                maximumOccurrences = booleanFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(IntFacet::class)) {
            val intFacet = facetClass.getAnnotationIncludingSuperclasses(IntFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.NUMBER,
                minimumOccurrences = intFacet.minimumOccurrences,
                maximumOccurrences = intFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(EnumFacet::class)) {
            val enumFacet = facetClass.getAnnotationIncludingSuperclasses(EnumFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.TEXT_ENUMERATION,
                minimumOccurrences = enumFacet.minimumOccurrences,
                maximumOccurrences = enumFacet.maximumOccurrences,
                enumerationType = enumFacet.enumerationClass,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(ReferenceFacet::class)) {
            val referenceFacet = facetClass.getAnnotationIncludingSuperclasses(ReferenceFacet::class)
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
            throw WrongTypeSyntaxException(SchemaErrorCode.FACET_ENUM_INVALID, facetName, conceptName, enumerationType ?: "null")
        }


        if(facetType == FacetType.REFERENCE && referencedConcepts.isEmpty()) {
            throw WrongTypeSyntaxException(SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST, facetName, conceptName)
        }

        if(facetType != FacetType.REFERENCE && referencedConcepts.isNotEmpty()) {
            throw WrongTypeSyntaxException(SchemaErrorCode.FACET_NOT_REFERENCE_NOT_EMPTY_CONCEPT_LIST, facetName, conceptName, facetType, referencedConcepts)
        }

        if(minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongCardinalitySchemaSyntaxException(SchemaErrorCode.NO_NEGATIVE_FACET_CARDINALITIES, facetName, conceptName, minimumOccurrences, maximumOccurrences)
        }

        if(minimumOccurrences > maximumOccurrences) {
            throw WrongCardinalitySchemaSyntaxException(SchemaErrorCode.WRONG_FACET_CARDINALITIES, facetName, conceptName, minimumOccurrences, maximumOccurrences)
        }

        referencedConcepts.forEach { referencedConcept ->
            if(!conceptNames.contains(referencedConcept)) {
                throw WrongTypeSyntaxException(SchemaErrorCode.FACET_UNKNOWN_REFERENCED_CONCEPT, facetName, conceptName, referencedConcept, conceptNames)
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
}
