package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.MissingReferencedConceptFacetValueException
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataReferenceFacetAndQueryConceptIdentifierTest {

    private interface SchemaWithConceptWithFacet {

        interface ConceptWithFacet {
            @Facet
            val conceptId: String

            @Facet
            val conceptReferenceFacetAsList: List<ConceptWithFacet>
        }

        @Facet
        val concepts: List<ConceptWithFacet>
    }


    @Builder
    @ExpectedRootAlias("root")
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
                    facetToModify = "conceptReferenceFacetAsList",
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
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddReferences::class,
                    ) { builder ->
                        builder.createConcept(selfReferencingConceptIdentifier)
                            .addReference(selfReferencingConceptIdentifier)
                    }
                }
            }

        assertEquals(1, schemaInstance.concepts.size)
        val concept = schemaInstance.concepts.first()
        assertEquals(selfReferencingConceptIdentifier.name, concept.conceptId)
        assertEquals(1, concept.conceptReferenceFacetAsList.size)
        assertEquals(selfReferencingConceptIdentifier.name, concept.conceptReferenceFacetAsList[0].conceptId)
    }

    @Test
    fun `test add main concept referencing two other concepts`() {
        val mainConceptIdentifier = ConceptIdentifier.of("Main-Id")
        val firstReferencedConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondReferencedConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")

        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddReferences::class,
                    ) { builder ->
                        builder.createConcept(mainConceptIdentifier)
                            .addReference(firstReferencedConceptIdentifier)
                            .addReference(secondReferencedConceptIdentifier)
                        builder.createConcept(firstReferencedConceptIdentifier)
                        builder.createConcept(secondReferencedConceptIdentifier)
                    }
                }
            }

        assertEquals(3, schemaInstance.concepts.size)
        val mainConcept = schemaInstance.concepts.first { it.conceptId == mainConceptIdentifier.name }
        assertEquals(mainConceptIdentifier.name, mainConcept.conceptId)
        assertEquals(2, mainConcept.conceptReferenceFacetAsList.size)

        val firstReferencedConcept = mainConcept.conceptReferenceFacetAsList[0]
        val secondReferencedConcept = mainConcept.conceptReferenceFacetAsList[1]

        assertEquals(firstReferencedConceptIdentifier.name, firstReferencedConcept.conceptId)
        assertEquals(secondReferencedConceptIdentifier.name, secondReferencedConcept.conceptId)

        assertEquals(0, firstReferencedConcept.conceptReferenceFacetAsList.size)
        assertEquals(0, secondReferencedConcept.conceptReferenceFacetAsList.size)
    }

    @Test
    fun `test three concept referencing each other in a circle`() {
        val firstConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")
        val thirdConceptIdentifier = ConceptIdentifier.of("Third-Referenced-Id")

        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddReferences::class,
                    ) { builder ->
                        builder.createConcept(firstConceptIdentifier)
                            .addReference(secondConceptIdentifier)
                        builder.createConcept(secondConceptIdentifier)
                            .addReference(thirdConceptIdentifier)
                        builder.createConcept(thirdConceptIdentifier)
                            .addReference(firstConceptIdentifier)
                    }
                }
            }

        assertEquals(3, schemaInstance.concepts.size)

        val firstConcept = schemaInstance.concepts.first { it.conceptId == firstConceptIdentifier.name }
        val secondConcept = schemaInstance.concepts.first { it.conceptId == secondConceptIdentifier.name }
        val thirdConcept = schemaInstance.concepts.first { it.conceptId == thirdConceptIdentifier.name }

        assertEquals(firstConceptIdentifier.name, firstConcept.conceptId)
        assertEquals(secondConceptIdentifier.name, secondConcept.conceptId)
        assertEquals(thirdConceptIdentifier.name, thirdConcept.conceptId)

        assertEquals(secondConceptIdentifier.name, firstConcept.conceptReferenceFacetAsList.first().conceptId)
        assertEquals(thirdConceptIdentifier.name, secondConcept.conceptReferenceFacetAsList.first().conceptId)
        assertEquals(firstConceptIdentifier.name, thirdConcept.conceptReferenceFacetAsList.first().conceptId)
    }

    @Test
    fun `test capability to return chains of referenced concept instances`() {
        val firstConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")
        val thirdConceptIdentifier = ConceptIdentifier.of("Third-Referenced-Id")

        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddReferences::class,
                    ) { builder ->
                        builder.createConcept(firstConceptIdentifier)
                            .addReference(secondConceptIdentifier)
                        builder.createConcept(secondConceptIdentifier)
                            .addReference(thirdConceptIdentifier)
                        builder.createConcept(thirdConceptIdentifier)
                            .addReference(firstConceptIdentifier)
                    }
                }
            }

        assertEquals(3, schemaInstance.concepts.size)

        val firstConcept = schemaInstance.concepts.first { it.conceptId == firstConceptIdentifier.name }

        assertEquals(
            secondConceptIdentifier.name,
            firstConcept
                .conceptReferenceFacetAsList.first()
                .conceptReferenceFacetAsList.first()
                .conceptReferenceFacetAsList.first()
                .conceptReferenceFacetAsList.first()
                .conceptId,
        )
    }

    @Test
    fun `test referencing an unknown concept instance throws an exception`() {
        val mainConceptId = ConceptIdentifier.of("Main-Id")
        val instantiatedConceptId = ConceptIdentifier.of("Instantiated-Referenced-Id")
        val uninstantiatedConceptId = ConceptIdentifier.of("Uninstantiated-Referenced-Id")

        assertThrows<MissingReferencedConceptFacetValueException> {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderToAddReferences::class,
                    ) { builder ->
                        builder.createConcept(mainConceptId)
                            .addReference(instantiatedConceptId)
                            .addReference(uninstantiatedConceptId)
                        builder.createConcept(instantiatedConceptId)
                    }
                }
            }
        }
    }
}
