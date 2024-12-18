package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderDataProvider
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataProviderTest {

    @Schema(concepts = [SchemaWithConceptWithFacet.ConceptWithFacet::class])
    private interface SchemaWithConceptWithFacet {

        @Concept(facets = [
            ConceptWithFacet.TextFacet::class,
            ConceptWithFacet.NumberFacet::class,
        ])
        interface ConceptWithFacet {
            @StringFacet
            interface TextFacet
            @IntFacet
            interface NumberFacet

            @QueryConceptIdentifierValue()
            fun getConceptId(): ConceptIdentifier

            @QueryFacetValue(facetClass = TextFacet::class)
            fun getTextValue(): String

            @QueryFacetValue(facetClass = NumberFacet::class)
            fun getNumberValue(): Int

        }

        @QueryConcepts(conceptClasses = [ConceptWithFacet::class])
        fun getConcepts(): List<ConceptWithFacet>
    }

    @BuilderDataProvider
    data class MyFacetTextData(private val text: String) {

        @Suppress("UNUSED")
        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class)
        fun getFacetText(): String {
            return text
        }
    }

    @BuilderDataProvider
    data class MyGenericFacetData<T>(
        private val facetData: T
    ) {

        /**
         * Returning a generic type is not supported.
         */
        @Suppress("UNUSED")
        fun getMyFacetValueAsGenericType(): T {
            return facetData
        }

        @Suppress("UNUSED")
        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class)
        fun getMyFacetValue(): String {
            return "$facetData"
        }

    }

    @BuilderDataProvider
    data class MyConceptWithTextAndNumberData(
        private val conceptId: ConceptIdentifier,
        private val text: String,
        private val number: Int,
    ) {

        @Suppress("UNUSED")
        @BuilderData
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "bar")
        fun createConcept() {
            // do nothing as everything is already done with annotations
        }

        @Suppress("UNUSED")
        @BuilderData
        @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun getConceptIdentifier(): ConceptIdentifier {
            return conceptId
        }

        @Suppress("UNUSED")
        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "bar", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class)
        fun getTextFacetValue(): String {
            return text
        }

        @Suppress("UNUSED")
        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "bar", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class)
        fun getNumberFacetValue(): Int {
            return number
        }

    }

    @Builder
    private interface BuilderMethodForDataProvider {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetFixedIntFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class, value = 23)
        fun doSomethingWithTheProvidedTextValue(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier,
            @ProvideBuilderData myDataParameter: MyFacetTextData,
        )

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetFixedIntFacetValue(conceptToModifyAlias = "foo", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class, value = 24)
        fun doSomethingWithTheProvidedTextValue(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier,
            @ProvideBuilderData myDataParameter: MyGenericFacetData<String>,
        )

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomethingWithTheProvidedConcept(
            @ProvideBuilderData myDataParameter: MyConceptWithTextAndNumberData,
        )
    }

    @Test
    fun `test passing different objects that provide data for new concepts and facet values into builder does not fail`() {
        val fromSimpleDataObjectId = ConceptIdentifier.of("From-simple-data-object-id")
        val fromGenericDataObjectId = ConceptIdentifier.of("From-generic-data-object-id")
        val fromNewConceptDataObjectId = ConceptIdentifier.of("From-new-data-object-id")

        val mySimpleFacetData = MyFacetTextData("hallo from simple facet")
        val myGenericFacetData = MyGenericFacetData("hallo from generic facet")
        val myNewConceptData = MyConceptWithTextAndNumberData(
            conceptId = fromNewConceptDataObjectId,
            text = "hallo new concept",
            number = 42
        )

        val schemaWithConcept = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodForDataProvider::class) { builder ->
                builder.doSomethingWithTheProvidedTextValue(fromSimpleDataObjectId, mySimpleFacetData)
                builder.doSomethingWithTheProvidedTextValue(fromGenericDataObjectId, myGenericFacetData)
                builder.doSomethingWithTheProvidedConcept(myNewConceptData)
            }
        }

        val conceptFromSimpleObject = schemaWithConcept.getConcepts().first { it.getConceptId() == fromSimpleDataObjectId  }
        assertEquals(fromSimpleDataObjectId, conceptFromSimpleObject.getConceptId())
        assertEquals("hallo from simple facet", conceptFromSimpleObject.getTextValue())
        assertEquals(23, conceptFromSimpleObject.getNumberValue())

        val conceptFromGenericObject = schemaWithConcept.getConcepts().first { it.getConceptId() == fromGenericDataObjectId  }
        assertEquals(fromGenericDataObjectId, conceptFromGenericObject.getConceptId())
        assertEquals("hallo from generic facet", conceptFromGenericObject.getTextValue())
        assertEquals(24, conceptFromGenericObject.getNumberValue())

        val conceptFromNewObject = schemaWithConcept.getConcepts().first { it.getConceptId() == fromNewConceptDataObjectId  }
        assertEquals(fromNewConceptDataObjectId, conceptFromNewObject.getConceptId())
        assertEquals("hallo new concept", conceptFromNewObject.getTextValue())
        assertEquals(42, conceptFromNewObject.getNumberValue())
    }

}