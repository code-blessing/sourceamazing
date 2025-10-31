package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongReferencedConceptFacetValueException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataMixedConceptReferenceFacetTest {

    private interface SchemaWithConceptWithFacet {

        @Facet
        @References([ConceptAlpha::class, ConceptBeta::class])
        val alphaAndBetaAsList: List<AlphaAndBeta>

        interface AlphaAndBeta {
            @Facet
            val id: String
        }

        interface ConceptAlpha: AlphaAndBeta

        interface ConceptBeta: AlphaAndBeta

        interface ConceptGamma
    }


    @Builder
    private interface BuilderToAddReferences {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptAlpha::class, declareConceptAlias = "alphaConcept")
        fun createAlphaConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "alphaConcept")
            conceptIdentifier: ConceptIdentifier,
        )

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptBeta::class, declareConceptAlias = "betaConcept")
        fun createBetaConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "betaConcept")
            conceptIdentifier: ConceptIdentifier,
        )

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptGamma::class, declareConceptAlias = "gammaConcept")
        fun createGammaConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "gammaConcept")
            conceptIdentifier: ConceptIdentifier,
        )

        @BuilderMethod
        fun addReference(
            @SetFacetValue(
                facetToModify = "AlphaAndBetaReferenceFacet",
                conceptToModifyAlias = "mainConcept",
                facetModificationRule = FacetModificationRule.ADD,
            )
            conceptIdentifier: ConceptIdentifier,
        ): BuilderToAddReferences
    }

    @Test
    fun `test mixed concept of alpha and beta references`() {
        val alpha1ConceptIdentifier = ConceptIdentifier.of("Alpha1-Id")
        val alpha2ConceptIdentifier = ConceptIdentifier.of("Alpha2-Id")
        val beta1ConceptIdentifier = ConceptIdentifier.of("Beta1-Id")
        val beta2ConceptIdentifier = ConceptIdentifier.of("Beta2-Id")
        val gamma1ConceptIdentifier = ConceptIdentifier.of("Gamma1-Id")
        val schemaInstance: SchemaWithConceptWithFacet =
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        rootConceptIdentifier,
                        BuilderToAddReferences::class
                    ) { builder ->
                        builder.createAlphaConcept(alpha1ConceptIdentifier)
                        builder.createAlphaConcept(alpha2ConceptIdentifier)
                        builder.createBetaConcept(beta1ConceptIdentifier)
                        builder.createBetaConcept(beta2ConceptIdentifier)
                        builder.createGammaConcept(gamma1ConceptIdentifier)

                        builder
                            .addReference(alpha1ConceptIdentifier)
                            .addReference(beta1ConceptIdentifier)
                            .addReference(alpha1ConceptIdentifier)
                            .addReference(alpha2ConceptIdentifier)
                    }
                }
            }
        val expectedConceptIdentifiers = listOf(alpha1ConceptIdentifier, beta1ConceptIdentifier, alpha1ConceptIdentifier, alpha2ConceptIdentifier).map { it.name }

        assertEquals(expectedConceptIdentifiers, schemaInstance.alphaAndBetaAsList.map { it.id})
    }

    @Test
    fun `test mixed concept of alpha and beta and invalid gamma references`() {
        val alpha1ConceptIdentifier = ConceptIdentifier.of("Alpha1-Id")
        val alpha2ConceptIdentifier = ConceptIdentifier.of("Alpha2-Id")
        val beta1ConceptIdentifier = ConceptIdentifier.of("Beta1-Id")
        val beta2ConceptIdentifier = ConceptIdentifier.of("Beta2-Id")
        val gamma1ConceptIdentifier = ConceptIdentifier.of("Gamma1-Id")

        assertThrows<WrongReferencedConceptFacetValueException> {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        rootConceptIdentifier,
                        BuilderToAddReferences::class
                    ) { builder ->
                        builder.createAlphaConcept(alpha1ConceptIdentifier)
                        builder.createAlphaConcept(alpha2ConceptIdentifier)
                        builder.createBetaConcept(beta1ConceptIdentifier)
                        builder.createBetaConcept(beta2ConceptIdentifier)
                        builder.createGammaConcept(gamma1ConceptIdentifier)

                        builder
                            .addReference(alpha1ConceptIdentifier)
                            .addReference(beta1ConceptIdentifier)
                            .addReference(alpha1ConceptIdentifier)
                            .addReference(alpha2ConceptIdentifier)
                            .addReference(gamma1ConceptIdentifier) // this reference is invalid
                    }
                }
            }
        }
    }

}
