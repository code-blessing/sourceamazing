package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataQueryConceptsTest {

    @Schema(concepts = [
        SchemaWithConcepts.ConceptAlpha::class,
        SchemaWithConcepts.ConceptBeta::class,
        SchemaWithConcepts.ConceptGamma::class,
    ])
    private interface SchemaWithConcepts {
        interface GreekLetterConcept

        @Concept(facets = [])
        interface ConceptAlpha: GreekLetterConcept

        @Concept(facets = [])
        interface ConceptBeta: GreekLetterConcept

        @Concept(facets = [])
        interface ConceptGamma: GreekLetterConcept

        @QueryConcepts(conceptClasses = [ConceptAlpha::class])
        fun getAlphaConcepts(): List<ConceptAlpha>

        @QueryConcepts(conceptClasses = [ConceptBeta::class])
        fun getBetaConcepts(): List<ConceptBeta>

        @QueryConcepts(conceptClasses = [ConceptGamma::class])
        fun getGammaConcepts(): List<ConceptGamma>

        @QueryConcepts(conceptClasses = [ConceptAlpha::class, ConceptBeta::class])
        fun getAlphaAndBetaConcepts(): List<Any>

        @QueryConcepts(conceptClasses = [ConceptBeta::class, ConceptGamma::class])
        fun getBetaAndGammaConcepts(): List<GreekLetterConcept>

        @QueryConcepts(conceptClasses = [ConceptAlpha::class, ConceptBeta::class, ConceptGamma::class])
        fun getAllConcepts(): List<GreekLetterConcept>
    }


    @Builder
    private interface BuilderToAddConcepts {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConcepts.ConceptAlpha::class)
        @SetRandomConceptIdentifierValue()
        fun createAlphaConcept()


        @BuilderMethod
        @NewConcept(concept = SchemaWithConcepts.ConceptBeta::class)
        @SetRandomConceptIdentifierValue()
        fun createBetaConcept()

        @BuilderMethod
        @NewConcept(concept = SchemaWithConcepts.ConceptGamma::class)
        @SetRandomConceptIdentifierValue()
        fun createGammaConcept()
    }

    @Test
    fun `test query concepts filtered by concept type`() {
        val numberOfAlphaConcepts = 42
        val numberOfBetaConcepts = 125
        val numberOfGammaConcepts = 1

        val schemaInstance: SchemaWithConcepts = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConcepts::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddConcepts::class) { builder ->
                repeat(numberOfAlphaConcepts) {
                    builder.createAlphaConcept()
                }
                repeat(numberOfBetaConcepts) {
                    builder.createBetaConcept()
                }
                repeat(numberOfGammaConcepts) {
                    builder.createGammaConcept()
                }
            }
        }

        assertEquals(numberOfAlphaConcepts, schemaInstance.getAlphaConcepts().size)
        assertEquals(numberOfBetaConcepts, schemaInstance.getBetaConcepts().size)
        assertEquals(numberOfGammaConcepts, schemaInstance.getGammaConcepts().size)

        assertEquals(numberOfAlphaConcepts + numberOfBetaConcepts + numberOfGammaConcepts, schemaInstance.getAllConcepts().size)
        assertEquals(numberOfAlphaConcepts + numberOfBetaConcepts, schemaInstance.getAlphaAndBetaConcepts().size)
        assertEquals(numberOfBetaConcepts + numberOfGammaConcepts, schemaInstance.getBetaAndGammaConcepts().size)
    }
}