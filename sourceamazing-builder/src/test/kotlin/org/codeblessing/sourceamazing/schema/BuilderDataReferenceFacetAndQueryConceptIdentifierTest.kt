package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.MissingReferencedConceptFacetValueException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataReferenceFacetAndQueryConceptIdentifierTest {

    @Schema(concepts = [SchemaWithConceptWithFacet.ConceptWithFacet::class])
    private interface SchemaWithConceptWithFacet {

        @Concept(facets = [
            ConceptWithFacet.ConceptReferenceFacet::class,
        ])
        interface ConceptWithFacet {
            @ReferenceFacet(minimumOccurrences = 0, maximumOccurrences = 2, referencedConcepts = [ConceptWithFacet::class])
            interface ConceptReferenceFacet

            @QueryConceptIdentifierValue
            fun getConceptId(): ConceptIdentifier

            @QueryFacetValue(ConceptReferenceFacet::class)
            fun getConceptReferenceFacet(): ConceptWithFacet?

            @QueryFacetValue(ConceptReferenceFacet::class)
            fun getConceptReferenceFacetAsAny(): Any?

            @QueryFacetValue(ConceptReferenceFacet::class)
            fun getConceptReferenceFacetAsList(): List<ConceptWithFacet>

            @QueryFacetValue(ConceptReferenceFacet::class)
            fun getConceptReferenceFacetAsAnyList(): List<Any>
        }

        @QueryConcepts(conceptClasses = [ConceptWithFacet::class])
        fun getConcepts(): List<ConceptWithFacet>
    }


    @Builder
    private interface BuilderToAddReferences {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
        fun createConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "myConcept")
            conceptIdentifier: ConceptIdentifier,
        ): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun addReference(
                @SetFacetValue(
                    facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.ConceptReferenceFacet::class,
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                @IgnoreNullFacetValue
                myReference: ConceptIdentifier?,
            ): NestedBuilder
        }
    }

    @Test
    fun `test add reference to itself`() {
        val selfReferencingConceptIdentifier = ConceptIdentifier.of("Self-Referencing-Id")
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                builder.createConcept(selfReferencingConceptIdentifier)
                    .addReference(selfReferencingConceptIdentifier)
            }
        }

        assertEquals(1, schemaInstance.getConcepts().size)
        val concept = schemaInstance.getConcepts().first()
        assertEquals(selfReferencingConceptIdentifier, concept.getConceptId())
        assertEquals(1, concept.getConceptReferenceFacetAsList().size)
        assertEquals(selfReferencingConceptIdentifier, concept.getConceptReferenceFacetAsList()[0].getConceptId())
        assertEquals(selfReferencingConceptIdentifier, (concept.getConceptReferenceFacetAsAnyList()[0] as SchemaWithConceptWithFacet.ConceptWithFacet).getConceptId())
        assertEquals(selfReferencingConceptIdentifier, concept.getConceptReferenceFacet()?.getConceptId())
        assertEquals(selfReferencingConceptIdentifier, (concept.getConceptReferenceFacetAsAny() as SchemaWithConceptWithFacet.ConceptWithFacet).getConceptId())
    }

    @Test
    fun `test add main concept referencing two other concepts`() {
        val mainConceptIdentifier = ConceptIdentifier.of("Main-Id")
        val firstReferencedConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondReferencedConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")

        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                builder.createConcept(mainConceptIdentifier)
                    .addReference(firstReferencedConceptIdentifier)
                    .addReference(secondReferencedConceptIdentifier)
                builder.createConcept(firstReferencedConceptIdentifier)
                builder.createConcept(secondReferencedConceptIdentifier)
            }
        }

        assertEquals(3, schemaInstance.getConcepts().size)
        val mainConcept = schemaInstance.getConcepts().first { it.getConceptId() == mainConceptIdentifier }
        assertEquals(mainConceptIdentifier, mainConcept.getConceptId())
        assertEquals(2, mainConcept.getConceptReferenceFacetAsList().size)

        assertEquals(firstReferencedConceptIdentifier, mainConcept.getConceptReferenceFacet()?.getConceptId())
        val firstReferencedConcept = mainConcept.getConceptReferenceFacetAsList()[0]
        val secondReferencedConcept = mainConcept.getConceptReferenceFacetAsList()[1]

        assertEquals(firstReferencedConceptIdentifier, firstReferencedConcept.getConceptId())
        assertEquals(secondReferencedConceptIdentifier, secondReferencedConcept.getConceptId())

        assertEquals(0, firstReferencedConcept.getConceptReferenceFacetAsList().size)
        assertEquals(0, secondReferencedConcept.getConceptReferenceFacetAsList().size)
    }

    @Test
    fun `test three concept referencing each other in a circle`() {
        val firstConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")
        val thirdConceptIdentifier = ConceptIdentifier.of("Third-Referenced-Id")

        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                builder.createConcept(firstConceptIdentifier)
                    .addReference(secondConceptIdentifier)
                builder.createConcept(secondConceptIdentifier)
                    .addReference(thirdConceptIdentifier)
                builder.createConcept(thirdConceptIdentifier)
                    .addReference(firstConceptIdentifier)
            }
        }

        assertEquals(3, schemaInstance.getConcepts().size)

        val firstConcept = schemaInstance.getConcepts().first { it.getConceptId() == firstConceptIdentifier }
        val secondConcept = schemaInstance.getConcepts().first { it.getConceptId() == secondConceptIdentifier }
        val thirdConcept = schemaInstance.getConcepts().first { it.getConceptId() == thirdConceptIdentifier }

        assertEquals(firstConceptIdentifier, firstConcept.getConceptId())
        assertEquals(secondConceptIdentifier, secondConcept.getConceptId())
        assertEquals(thirdConceptIdentifier, thirdConcept.getConceptId())

        assertEquals(secondConceptIdentifier, firstConcept.getConceptReferenceFacet()?.getConceptId())
        assertEquals(thirdConceptIdentifier, secondConcept.getConceptReferenceFacet()?.getConceptId())
        assertEquals(firstConceptIdentifier, thirdConcept.getConceptReferenceFacet()?.getConceptId())
    }

    @Test
    fun `test capability to return chains of referenced concept instances`() {
        val firstConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")
        val thirdConceptIdentifier = ConceptIdentifier.of("Third-Referenced-Id")

        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                builder.createConcept(firstConceptIdentifier)
                    .addReference(secondConceptIdentifier)
                builder.createConcept(secondConceptIdentifier)
                    .addReference(thirdConceptIdentifier)
                builder.createConcept(thirdConceptIdentifier)
                    .addReference(firstConceptIdentifier)
            }
        }

        assertEquals(3, schemaInstance.getConcepts().size)

        val firstConcept = schemaInstance.getConcepts().first { it.getConceptId() == firstConceptIdentifier }

        assertEquals(secondConceptIdentifier, firstConcept
            .getConceptReferenceFacet()
            ?.getConceptReferenceFacet()
            ?.getConceptReferenceFacet()
            ?.getConceptReferenceFacet()
            ?.getConceptId())
    }

    @Test
    fun `test referencing an unknown concept instance throws an exception`() {
        val mainConceptId = ConceptIdentifier.of("Main-Id")
        val instantiatedConceptId = ConceptIdentifier.of("Instantiated-Referenced-Id")
        val uninstantiatedConceptId = ConceptIdentifier.of("Uninstantiated-Referenced-Id")

        assertThrows<MissingReferencedConceptFacetValueException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder.createConcept(mainConceptId)
                        .addReference(instantiatedConceptId)
                        .addReference(uninstantiatedConceptId)
                    builder.createConcept(instantiatedConceptId)
                }
            }
        }
    }
}