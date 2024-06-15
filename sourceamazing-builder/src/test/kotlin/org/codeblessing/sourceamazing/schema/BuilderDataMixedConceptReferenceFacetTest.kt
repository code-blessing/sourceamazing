package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
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
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongReferencedConceptFacetValueException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataMixedConceptReferenceFacetTest {

    @Schema(concepts = [
        SchemaWithConceptWithFacet.MainConcept::class,
        SchemaWithConceptWithFacet.ConceptAlpha::class,
        SchemaWithConceptWithFacet.ConceptBeta::class,
        SchemaWithConceptWithFacet.ConceptGamma::class,
    ])
    private interface SchemaWithConceptWithFacet {
        @Concept(facets = [
            MainConcept.AlphaAndBetaReferenceFacet::class,
        ])
        interface MainConcept {
            @ReferenceFacet(minimumOccurrences = 0, maximumOccurrences = 10, referencedConcepts = [ConceptAlpha::class, ConceptBeta::class])
            interface AlphaAndBetaReferenceFacet

            @QueryFacetValue(AlphaAndBetaReferenceFacet::class)
            fun getAlphaAndBetaAsList(): List<AlphaAndBeta>
        }

        interface AlphaAndBeta {
            @QueryConceptIdentifierValue
            fun getAlphaOrBetaConceptId(): ConceptIdentifier
        }

        @Concept(facets = [])
        interface ConceptAlpha: AlphaAndBeta

        @Concept(facets = [])
        interface ConceptBeta: AlphaAndBeta

        @Concept(facets = [])
        interface ConceptGamma

        @QueryConcepts(conceptClasses = [MainConcept::class])
        fun getMainConcepts(): List<MainConcept>
    }


    @Builder
    private interface BuilderToAddReferences {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.MainConcept::class, declareConceptAlias = "mainConcept")
        fun createMainConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "mainConcept")
            conceptIdentifier: ConceptIdentifier,
        ): NestedBuilder

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

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "mainConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun addReference(
                @SetFacetValue(
                    facetToModify = SchemaWithConceptWithFacet.MainConcept.AlphaAndBetaReferenceFacet::class,
                    conceptToModifyAlias = "mainConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                conceptIdentifier: ConceptIdentifier,
            ): NestedBuilder
        }
    }

    @Test
    fun `test mixed concept of alpha and beta references`() {
        val mainConceptIdentifier = ConceptIdentifier.of("Main-Id")
        val alpha1ConceptIdentifier = ConceptIdentifier.of("Alpha1-Id")
        val alpha2ConceptIdentifier = ConceptIdentifier.of("Alpha2-Id")
        val beta1ConceptIdentifier = ConceptIdentifier.of("Beta1-Id")
        val beta2ConceptIdentifier = ConceptIdentifier.of("Beta2-Id")
        val gamma1ConceptIdentifier = ConceptIdentifier.of("Gamma1-Id")
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                builder.createAlphaConcept(alpha1ConceptIdentifier)
                builder.createAlphaConcept(alpha2ConceptIdentifier)
                builder.createBetaConcept(beta1ConceptIdentifier)
                builder.createBetaConcept(beta2ConceptIdentifier)
                builder.createGammaConcept(gamma1ConceptIdentifier)

                builder.createMainConcept(mainConceptIdentifier)
                    .addReference(alpha1ConceptIdentifier)
                    .addReference(beta1ConceptIdentifier)
                    .addReference(alpha1ConceptIdentifier)
                    .addReference(alpha2ConceptIdentifier)
            }
        }
        val expectedConceptIdentifiers = listOf(alpha1ConceptIdentifier, beta1ConceptIdentifier, alpha1ConceptIdentifier, alpha2ConceptIdentifier)

        assertEquals(1, schemaInstance.getMainConcepts().size)
        val mainConcept = schemaInstance.getMainConcepts().first()
        assertEquals(expectedConceptIdentifiers, mainConcept.getAlphaAndBetaAsList().map { it.getAlphaOrBetaConceptId()})
    }

    @Test
    fun `test mixed concept of alpha and beta and invalid gamma references`() {
        val mainConceptIdentifier = ConceptIdentifier.of("Main-Id")
        val alpha1ConceptIdentifier = ConceptIdentifier.of("Alpha1-Id")
        val alpha2ConceptIdentifier = ConceptIdentifier.of("Alpha2-Id")
        val beta1ConceptIdentifier = ConceptIdentifier.of("Beta1-Id")
        val beta2ConceptIdentifier = ConceptIdentifier.of("Beta2-Id")
        val gamma1ConceptIdentifier = ConceptIdentifier.of("Gamma1-Id")

        assertThrows<WrongReferencedConceptFacetValueException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder.createAlphaConcept(alpha1ConceptIdentifier)
                    builder.createAlphaConcept(alpha2ConceptIdentifier)
                    builder.createBetaConcept(beta1ConceptIdentifier)
                    builder.createBetaConcept(beta2ConceptIdentifier)
                    builder.createGammaConcept(gamma1ConceptIdentifier)

                    builder.createMainConcept(mainConceptIdentifier)
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