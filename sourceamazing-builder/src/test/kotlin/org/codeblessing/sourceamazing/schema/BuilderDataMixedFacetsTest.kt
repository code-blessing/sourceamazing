package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataMixedFacetsTest {

    @Schema(concepts = [
        SchemaWithConceptWithFacet.ConceptOneUsingSharedFacet::class,
        SchemaWithConceptWithFacet.ConceptTwoUsingSharedFacet::class,
    ])
    private interface SchemaWithConceptWithFacet {

        @StringFacet
        interface SharedFacet

        @IntFacet
        interface NotSharedFacet

        @Concept(facets = [
            SharedFacet::class,
            NotSharedFacet::class,
        ])
        interface ConceptOneUsingSharedFacet {
            @QueryFacetValue(SharedFacet::class)
            fun getSharedFacetValue(): String
            @QueryFacetValue(NotSharedFacet::class)
            fun getNotSharedFacetValue(): Int
        }

        @Concept(facets = [
            SharedFacet::class,
        ])
        interface ConceptTwoUsingSharedFacet {
            @QueryFacetValue(SharedFacet::class)
            fun getSharedFacetValueForConceptTwo(): String
        }

        @QueryConcepts(conceptClasses = [ConceptOneUsingSharedFacet::class, ConceptTwoUsingSharedFacet::class])
        fun getAllConcepts(): List<Any>

        @QueryConcepts(conceptClasses = [ConceptOneUsingSharedFacet::class])
        fun getAllConceptOne(): List<ConceptOneUsingSharedFacet>

        @QueryConcepts(conceptClasses = [ConceptTwoUsingSharedFacet::class])
        fun getAllConceptTwo(): List<ConceptTwoUsingSharedFacet>
    }


    @Builder
    private interface BuilderToAddConcepts {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptOneUsingSharedFacet::class, declareConceptAlias = "conceptWithSharedFacet")
        @SetRandomConceptIdentifierValue("conceptWithSharedFacet")
        fun createConceptOne(
            @SetFacetValue(conceptToModifyAlias = "conceptWithSharedFacet", facetToModify = SchemaWithConceptWithFacet.NotSharedFacet::class)
            myValueForNotSharedFacet: Int
        ): NestedSharedBuilder

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptTwoUsingSharedFacet::class, declareConceptAlias = "conceptWithSharedFacet")
        @SetRandomConceptIdentifierValue("conceptWithSharedFacet")
        fun createConceptTwo(): NestedSharedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "conceptWithSharedFacet")
        interface NestedSharedBuilder {
            @BuilderMethod
            fun addSharedValue(
                @SetFacetValue(conceptToModifyAlias = "conceptWithSharedFacet", facetToModify = SchemaWithConceptWithFacet.SharedFacet::class)
                mySharedValue: String
            ): NestedSharedBuilder
        }
    }

    @Test
    fun `test mixed concept of alpha and beta references`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddConcepts::class) { builder ->
                builder.createConceptOne(42).addSharedValue("SharedValueForConceptOne")
                builder.createConceptTwo().addSharedValue("SharedValueForConceptTwo1")
                builder.createConceptTwo().addSharedValue("SharedValueForConceptTwo2")
            }
        }
        assertEquals(3, schemaInstance.getAllConcepts().size)

        val conceptOne = schemaInstance.getAllConceptOne().first()
        assertEquals(42, conceptOne.getNotSharedFacetValue())
        assertEquals("SharedValueForConceptOne", conceptOne.getSharedFacetValue())

        val firstConceptTwo = schemaInstance.getAllConceptTwo().first()
        assertEquals("SharedValueForConceptTwo1", firstConceptTwo.getSharedFacetValueForConceptTwo())

        val secondConceptTwo = schemaInstance.getAllConceptTwo().last()
        assertEquals("SharedValueForConceptTwo2", secondConceptTwo.getSharedFacetValueForConceptTwo())
    }
}