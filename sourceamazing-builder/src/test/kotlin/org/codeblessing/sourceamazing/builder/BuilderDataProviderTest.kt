package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.toConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.withDefaultValueRootInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderDataProviderTest {

    private interface MyConcepts {

        interface MyConcept {
            val id: String

            val text: String

            val number: Int
        }

        val concepts: List<MyConcept>
    }

    @BuilderDataProvider
    data class MyFacetTextData(private val text: String) {

        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "text")
        fun getFacetText(): String {
            return text
        }
    }

    @BuilderDataProvider
    data class MyGenericFacetData<T>(private val facetData: T) {

        /** Returning a generic type is not supported. */
        fun getMyFacetValueAsGenericType(): T {
            return facetData
        }

        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "foo", facetToModify = "text")
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

        @BuilderData
        @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "bar")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "bar",
        )
        fun createConcept() {
            // do nothing as everything is already done with annotations
        }

        @BuilderData
        @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "bar")
        fun getConceptIdentifier(): ConceptIdentifier {
            return conceptId
        }

        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "bar", facetToModify = "id")
        fun getId(): String {
            return conceptId.name
        }

        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "bar", facetToModify = "text")
        fun getTextFacetValue(): String {
            return text
        }

        @BuilderData
        @SetProvidedFacetValue(conceptToModifyAlias = "bar", facetToModify = "number")
        fun getNumberFacetValue(): Int {
            return number
        }
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderMethodForDataProvider {

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "foo",
        )
        @SetFixedIntFacetValue(conceptToModifyAlias = "foo", facetToModify = "number", value = 23)
        fun doSomethingWithTheProvidedTextValue(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "id") id: String,
            @ProvideBuilderData myDataParameter: MyFacetTextData,
        )

        @BuilderMethod
        @NewConcept(MyConcepts.MyConcept::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "foo",
        )
        @SetFixedIntFacetValue(conceptToModifyAlias = "foo", facetToModify = "number", value = 24)
        fun doSomethingWithTheProvidedTextValue(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptId: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "foo", facetToModify = "id") id: String,
            @ProvideBuilderData myDataParameter: MyGenericFacetData<String>,
        )

        @BuilderMethod
        fun doSomethingWithTheProvidedConcept(@ProvideBuilderData myDataParameter: MyConceptWithTextAndNumberData)
    }

    @Test
    fun `test passing different objects that provide data for new concepts and facet values into builder does not fail`() {
        val fromSimpleDataObjectId = ConceptIdentifier.of("From-simple-data-object-id")
        val fromGenericDataObjectId = ConceptIdentifier.of("From-generic-data-object-id")
        val fromNewConceptDataObjectId = ConceptIdentifier.of("From-new-data-object-id")

        val mySimpleFacetData = MyFacetTextData("hallo from simple facet")
        val myGenericFacetData = MyGenericFacetData("hallo from generic facet")
        val myNewConceptData =
            MyConceptWithTextAndNumberData(
                conceptId = fromNewConceptDataObjectId,
                text = "hallo new concept",
                number = 42,
            )

        val schemaWithConcept =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<MyConcepts> { conceptData ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodForDataProvider::class,
                        mapOf("root" to conceptData.toConceptNameAndIdentifier()),
                    ) { builder ->
                        builder.doSomethingWithTheProvidedTextValue(
                            fromSimpleDataObjectId,
                            fromSimpleDataObjectId.name,
                            mySimpleFacetData,
                        )
                        builder.doSomethingWithTheProvidedTextValue(
                            fromGenericDataObjectId,
                            fromGenericDataObjectId.name,
                            myGenericFacetData,
                        )
                        builder.doSomethingWithTheProvidedConcept(myNewConceptData)
                    }
                }
            }

        val conceptFromSimpleObject = schemaWithConcept.concepts.first { it.id == fromSimpleDataObjectId.name }
        assertEquals(fromSimpleDataObjectId.name, conceptFromSimpleObject.id)
        assertEquals("hallo from simple facet", conceptFromSimpleObject.text)
        assertEquals(23, conceptFromSimpleObject.number)

        val conceptFromGenericObject = schemaWithConcept.concepts.first { it.id == fromGenericDataObjectId.name }
        assertEquals(fromGenericDataObjectId.name, conceptFromGenericObject.id)
        assertEquals("hallo from generic facet", conceptFromGenericObject.text)
        assertEquals(24, conceptFromGenericObject.number)

        val conceptFromNewObject = schemaWithConcept.concepts.first { it.id == fromNewConceptDataObjectId.name }
        assertEquals(fromNewConceptDataObjectId.name, conceptFromNewObject.id)
        assertEquals("hallo new concept", conceptFromNewObject.text)
        assertEquals(42, conceptFromNewObject.number)
    }
}
