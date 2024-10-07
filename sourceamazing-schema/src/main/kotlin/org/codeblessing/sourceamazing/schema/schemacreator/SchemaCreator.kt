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
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateConceptSchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateFacetSchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalitySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.query.ConceptQueryValidator
import org.codeblessing.sourceamazing.schema.schemacreator.query.SchemaQueryValidator
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.TypeCheckerUtil.checkHasAnnotation
import org.codeblessing.sourceamazing.schema.type.TypeCheckerUtil.checkHasExactNumberOfAnnotations
import org.codeblessing.sourceamazing.schema.type.TypeCheckerUtil.checkHasExactlyOneOfAnnotation
import org.codeblessing.sourceamazing.schema.type.TypeCheckerUtil.checkHasNoGenericTypeParameters
import org.codeblessing.sourceamazing.schema.type.TypeCheckerUtil.checkHasOnlyAnnotation
import org.codeblessing.sourceamazing.schema.type.TypeCheckerUtil.checkIsOrdinaryInterface
import org.codeblessing.sourceamazing.schema.type.getAnnotation
import org.codeblessing.sourceamazing.schema.type.hasAnnotation
import org.codeblessing.sourceamazing.schema.type.isEnum
import kotlin.reflect.KClass

object SchemaCreator {
    private const val SCHEMA_CLASS_DESCRIPTION = "Schema Definition Class"
    private const val CONCEPT_CLASS_DESCRIPTION = "Concept Class"
    private const val FACET_CLASS_DESCRIPTION = "Facet Class"

    @Throws(SyntaxException::class)
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
                throw DuplicateConceptSchemaSyntaxException("There is already a concept registered " +
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
                    throw DuplicateFacetSchemaSyntaxException("There is already a facet registered " +
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
        checkIsOrdinaryInterface(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasNoGenericTypeParameters(schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
        checkHasExactNumberOfAnnotations(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION, numberOf = 1)
        checkHasOnlyAnnotation(Schema::class, schemaDefinitionClass, SCHEMA_CLASS_DESCRIPTION)
    }

    private fun validateConceptClassesAnnotations(conceptClasses: List<KClass<*>>) {
        conceptClasses.forEach { conceptClass ->
            checkIsOrdinaryInterface(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasNoGenericTypeParameters(conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
            checkHasExactNumberOfAnnotations(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION, numberOf = 1)
            checkHasOnlyAnnotation(Concept::class, conceptClass, CONCEPT_CLASS_DESCRIPTION)
        }
    }

    private fun validateFacetClassesAnnotations(facetClasses: Collection<KClass<*>>) {
        facetClasses.forEach { facetClass ->
            checkIsOrdinaryInterface(facetClass, FACET_CLASS_DESCRIPTION)
            checkHasNoGenericTypeParameters(facetClass, FACET_CLASS_DESCRIPTION)
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
            throw WrongTypeSyntaxException(
                "Facet '$facetName' on concept '$conceptName' " +
                        "is declared as type '${FacetType.TEXT_ENUMERATION}' but the enumeration is not " +
                        "defined or not a real enumeration class (was '$enumerationType')."
            )
        }


        if(facetType == FacetType.REFERENCE && referencedConcepts.isEmpty()) {
            throw WrongTypeSyntaxException(
                "Facet '$facetName' on concept '$conceptName' " +
                "is declared as type '${FacetType.REFERENCE}' " +
                "but the list of concept type is empty."
            )
        }

        if(facetType != FacetType.REFERENCE && referencedConcepts.isNotEmpty()) {
            throw WrongTypeSyntaxException(
                "Facet '$facetName' on concept '$conceptName' " +
                "is not declared as type '${FacetType.REFERENCE}' (is '${facetType}') but the list " +
                "of concept type is not empty (is '${referencedConcepts}')."
            )
        }

        if(minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongCardinalitySchemaSyntaxException(
                "Facet '$facetName' on concept '$conceptName' " +
                "has negative cardinalities. Only number greater/equal zero are allowed, " +
                "but was $minimumOccurrences/$maximumOccurrences."
            )
        }

        if(minimumOccurrences > maximumOccurrences) {
            throw WrongCardinalitySchemaSyntaxException(
                "Facet '$facetName' on concept '$conceptName' " +
                "has a greater minimumOccurrences ($minimumOccurrences) " +
                "than the maximumOccurrences ($maximumOccurrences)."
            )
        }

        referencedConcepts.forEach { referencedConcept ->
            if(!conceptNames.contains(referencedConcept)) {
                throw WrongTypeSyntaxException(
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
}
