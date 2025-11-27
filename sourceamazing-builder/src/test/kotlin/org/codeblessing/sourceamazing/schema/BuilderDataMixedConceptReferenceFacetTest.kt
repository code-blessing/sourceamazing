package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.WrongReferencedConceptFacetValueException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED")
class BuilderDataMixedConceptReferenceFacetTest {

    private interface MyConcepts {

        @References([ConceptAlpha::class, ConceptBeta::class])
        val alphaAndBetaAsList: List<AlphaAndBeta>

        val gammaAsList: List<ConceptGamma>

        interface AlphaAndBeta {
            val id: String
        }

        interface ConceptAlpha : AlphaAndBeta

        interface ConceptBeta : AlphaAndBeta

        interface ConceptGamma
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(MyConcepts::class, "root")
    private interface BuilderToAddReferences {

        @BuilderMethod
        @NewConcept(concept = MyConcepts.ConceptAlpha::class, declareConceptAlias = "alphaConcept")
        fun createAlphaConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "alphaConcept")
            conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "alphaConcept", facetToModify = "id") id: String,
        )

        @BuilderMethod
        @NewConcept(concept = MyConcepts.ConceptBeta::class, declareConceptAlias = "betaConcept")
        fun createBetaConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "betaConcept")
            conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "betaConcept", facetToModify = "id") id: String,
        )

        @BuilderMethod
        @NewConcept(concept = MyConcepts.ConceptGamma::class, declareConceptAlias = "gammaConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "gammaAsList",
            referencedConceptAlias = "gammaConcept",
        )
        fun createGammaConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "gammaConcept")
            conceptIdentifier: ConceptIdentifier
        )

        @BuilderMethod
        fun addReference(
            @SetFacetValue(
                conceptToModifyAlias = "root",
                facetToModify = "alphaAndBetaAsList",
                facetModificationRule = FacetModificationRule.ADD,
            )
            conceptIdentifier: ConceptIdentifier
        )
    }

    @Test
    fun `test mixed concept of alpha and beta references`() {
        val alpha1ConceptIdentifier = ConceptIdentifier.of("Alpha1-Id")
        val alpha2ConceptIdentifier = ConceptIdentifier.of("Alpha2-Id")
        val beta1ConceptIdentifier = ConceptIdentifier.of("Beta1-Id")
        val beta2ConceptIdentifier = ConceptIdentifier.of("Beta2-Id")
        val gamma1ConceptIdentifier = ConceptIdentifier.of("Gamma1-Id")
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddReferences::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createAlphaConcept(
                            alpha1ConceptIdentifier,
                            alpha1ConceptIdentifier.name,
                        )
                        builder.createAlphaConcept(
                            alpha2ConceptIdentifier,
                            alpha2ConceptIdentifier.name,
                        )
                        builder.createBetaConcept(
                            beta1ConceptIdentifier,
                            beta1ConceptIdentifier.name,
                        )
                        builder.createBetaConcept(
                            beta2ConceptIdentifier,
                            beta2ConceptIdentifier.name,
                        )
                        builder.createGammaConcept(gamma1ConceptIdentifier)

                        builder.addReference(alpha1ConceptIdentifier)
                        builder.addReference(beta1ConceptIdentifier)
                        builder.addReference(alpha1ConceptIdentifier)
                        builder.addReference(alpha2ConceptIdentifier)
                    }
                }
            }
        val expectedConceptIdentifiers =
            listOf(
                    alpha1ConceptIdentifier,
                    beta1ConceptIdentifier,
                    alpha1ConceptIdentifier,
                    alpha2ConceptIdentifier,
                )
                .map { it.name }

        assertEquals(expectedConceptIdentifiers, schemaInstance.alphaAndBetaAsList.map { it.id })
    }

    @Test
    fun `test mixed concept of alpha and beta and invalid gamma references`() {
        val alpha1ConceptIdentifier = ConceptIdentifier.of("Alpha1-Id")
        val alpha2ConceptIdentifier = ConceptIdentifier.of("Alpha2-Id")
        val beta1ConceptIdentifier = ConceptIdentifier.of("Beta1-Id")
        val beta2ConceptIdentifier = ConceptIdentifier.of("Beta2-Id")
        val gamma1ConceptIdentifier = ConceptIdentifier.of("Gamma1-Id")

        assertThrows<WrongReferencedConceptFacetValueException> {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddReferences::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createAlphaConcept(
                            alpha1ConceptIdentifier,
                            alpha1ConceptIdentifier.name,
                        )
                        builder.createAlphaConcept(
                            alpha2ConceptIdentifier,
                            alpha2ConceptIdentifier.name,
                        )
                        builder.createBetaConcept(
                            beta1ConceptIdentifier,
                            beta1ConceptIdentifier.name,
                        )
                        builder.createBetaConcept(
                            beta2ConceptIdentifier,
                            beta2ConceptIdentifier.name,
                        )
                        builder.createGammaConcept(gamma1ConceptIdentifier)

                        builder.addReference(alpha1ConceptIdentifier)
                        builder.addReference(beta1ConceptIdentifier)
                        builder.addReference(alpha1ConceptIdentifier)
                        builder.addReference(alpha2ConceptIdentifier)
                        builder.addReference(gamma1ConceptIdentifier) // this reference is invalid
                    }
                }
            }
        }
    }
}
