package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.MissingReferencedConceptFacetValueException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataReferenceFacetAndQueryConceptIdentifierTest {

    private interface MyConcepts {

        interface MyConcept {
            val id: String

            val conceptReferences: List<MyConcept>
        }

        val concepts: List<MyConcept>
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderToAddReferences {

        @BuilderMethod
        @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "myConcept") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "id") id: String,
        ): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.MyConcept::class, conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun addReference(
                @SetFacetValue(
                    facetToModify = "conceptReferences",
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                @IgnoreNullFacetValue
                myReference: ConceptIdentifier?
            ): NestedBuilder
        }
    }

    @Test
    fun `test add reference to itself`() {
        val selfReferencingConceptIdentifier = ConceptIdentifier.of("Self-Referencing-Id")
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddReferences::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept(selfReferencingConceptIdentifier, selfReferencingConceptIdentifier.name)
                            .addReference(selfReferencingConceptIdentifier)
                    }
                }
            }

        assertEquals(1, schemaInstance.concepts.size)
        val concept = schemaInstance.concepts.first()
        assertEquals(selfReferencingConceptIdentifier.name, concept.id)
        assertEquals(1, concept.conceptReferences.size)
        assertEquals(selfReferencingConceptIdentifier.name, concept.conceptReferences[0].id)
    }

    @Test
    fun `test add main concept referencing two other concepts`() {
        val mainConceptIdentifier = ConceptIdentifier.of("Main-Id")
        val firstReferencedConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondReferencedConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")

        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddReferences::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept(mainConceptIdentifier, mainConceptIdentifier.name)
                            .addReference(firstReferencedConceptIdentifier)
                            .addReference(secondReferencedConceptIdentifier)
                        builder.createConcept(firstReferencedConceptIdentifier, firstReferencedConceptIdentifier.name)
                        builder.createConcept(secondReferencedConceptIdentifier, secondReferencedConceptIdentifier.name)
                    }
                }
            }

        assertEquals(3, schemaInstance.concepts.size)
        val mainConcept = schemaInstance.concepts.first { it.id == mainConceptIdentifier.name }
        assertEquals(mainConceptIdentifier.name, mainConcept.id)
        assertEquals(2, mainConcept.conceptReferences.size)

        val firstReferencedConcept = mainConcept.conceptReferences[0]
        val secondReferencedConcept = mainConcept.conceptReferences[1]

        assertEquals(firstReferencedConceptIdentifier.name, firstReferencedConcept.id)
        assertEquals(secondReferencedConceptIdentifier.name, secondReferencedConcept.id)

        assertEquals(0, firstReferencedConcept.conceptReferences.size)
        assertEquals(0, secondReferencedConcept.conceptReferences.size)
    }

    @Test
    fun `test three concept referencing each other in a circle`() {
        val firstConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")
        val thirdConceptIdentifier = ConceptIdentifier.of("Third-Referenced-Id")

        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddReferences::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept(firstConceptIdentifier, firstConceptIdentifier.name)
                            .addReference(secondConceptIdentifier)
                        builder
                            .createConcept(secondConceptIdentifier, secondConceptIdentifier.name)
                            .addReference(thirdConceptIdentifier)
                        builder
                            .createConcept(thirdConceptIdentifier, thirdConceptIdentifier.name)
                            .addReference(firstConceptIdentifier)
                    }
                }
            }

        assertEquals(3, schemaInstance.concepts.size)

        val firstConcept = schemaInstance.concepts.first { it.id == firstConceptIdentifier.name }
        val secondConcept = schemaInstance.concepts.first { it.id == secondConceptIdentifier.name }
        val thirdConcept = schemaInstance.concepts.first { it.id == thirdConceptIdentifier.name }

        assertEquals(firstConceptIdentifier.name, firstConcept.id)
        assertEquals(secondConceptIdentifier.name, secondConcept.id)
        assertEquals(thirdConceptIdentifier.name, thirdConcept.id)

        assertEquals(secondConceptIdentifier.name, firstConcept.conceptReferences.first().id)
        assertEquals(thirdConceptIdentifier.name, secondConcept.conceptReferences.first().id)
        assertEquals(firstConceptIdentifier.name, thirdConcept.conceptReferences.first().id)
    }

    @Test
    fun `test capability to return chains of referenced concept instances`() {
        val firstConceptIdentifier = ConceptIdentifier.of("First-Referenced-Id")
        val secondConceptIdentifier = ConceptIdentifier.of("Second-Referenced-Id")
        val thirdConceptIdentifier = ConceptIdentifier.of("Third-Referenced-Id")

        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddReferences::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept(firstConceptIdentifier, firstConceptIdentifier.name)
                            .addReference(secondConceptIdentifier)
                        builder
                            .createConcept(secondConceptIdentifier, secondConceptIdentifier.name)
                            .addReference(thirdConceptIdentifier)
                        builder
                            .createConcept(thirdConceptIdentifier, thirdConceptIdentifier.name)
                            .addReference(firstConceptIdentifier)
                    }
                }
            }

        assertEquals(3, schemaInstance.concepts.size)

        val firstConcept = schemaInstance.concepts.first { it.id == firstConceptIdentifier.name }

        assertEquals(
            secondConceptIdentifier.name,
            firstConcept.conceptReferences
                .first()
                .conceptReferences
                .first()
                .conceptReferences
                .first()
                .conceptReferences
                .first()
                .id,
        )
    }

    @Test
    fun `test referencing an unknown concept instance throws an exception`() {
        val mainConceptId = ConceptIdentifier.of("Main-Id")
        val instantiatedConceptId = ConceptIdentifier.of("Instantiated-Referenced-Id")
        val uninstantiatedConceptId = ConceptIdentifier.of("Uninstantiated-Referenced-Id")

        assertThrows<MissingReferencedConceptFacetValueException> {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddReferences::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder
                            .createConcept(mainConceptId, mainConceptId.name)
                            .addReference(instantiatedConceptId)
                            .addReference(uninstantiatedConceptId)
                        builder.createConcept(instantiatedConceptId, instantiatedConceptId.name)
                    }
                }
            }
        }
    }
}
