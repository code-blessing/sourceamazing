package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DataCollectorBuilderNullValueInvocationHandlerTest {

    @Schema(concepts = [
        TestSchema.TestConcept::class,
    ])
    interface TestSchema {

        @Concept(facets = [
            TestConcept.NullableValueFacet::class,
        ])
        interface TestConcept {
            @StringFacet(minimumOccurrences = 0, maximumOccurrences = 3)
            interface NullableValueFacet

            @QueryFacetValue(NullableValueFacet::class)
            fun getNullableValue(): String?
        }

        @QueryConcepts([TestConcept::class])
        fun getTestConcepts(): List<TestConcept>
    }

    @Builder
    interface TestBuilder {

        @BuilderMethod
        @NewConcept(concept = TestSchema.TestConcept::class, declareConceptAlias = "testConcept")
        fun newTestConcept(
            @SetConceptIdentifierValue(conceptToModifyAlias = "testConcept")
            conceptIdentifier: ConceptIdentifier?,
            @SetFacetValue(facetToModify = TestSchema.TestConcept.NullableValueFacet::class, conceptToModifyAlias = "testConcept")
            myNullableValue: String?,
        )

        @BuilderMethod
        @NewConcept(concept = TestSchema.TestConcept::class, declareConceptAlias = "testConcept")
        fun newTestConceptIgnoringNullValues(
            @SetConceptIdentifierValue(conceptToModifyAlias = "testConcept")
            conceptIdentifier: ConceptIdentifier?,
            @SetFacetValue(facetToModify = TestSchema.TestConcept.NullableValueFacet::class, conceptToModifyAlias = "testConcept")
            @IgnoreNullFacetValue
            myNullableValue: String?,
        )

        @BuilderMethod
        @NewConcept(concept = TestSchema.TestConcept::class, declareConceptAlias = "testConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "testConcept")
        @WithNewBuilder(TestSubBuilder::class)
        fun newTestConceptWithSubDsl(
            @SetFacetValue(facetToModify = TestSchema.TestConcept.NullableValueFacet::class, conceptToModifyAlias = "testConcept")
            myNullableValue: String?,
            @InjectBuilder subBuilder: (TestSubBuilder.() -> Unit)?,
        )

    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(conceptAlias = "testConcept")
    interface TestSubBuilder {
        @BuilderMethod
        fun changeNullableValue(
            @SetFacetValue(facetToModify = TestSchema.TestConcept.NullableValueFacet::class, conceptToModifyAlias = "testConcept", facetModificationRule = FacetModificationRule.REPLACE)
            @IgnoreNullFacetValue
            myNullableValue: String?,
        )

    }

    private val firstId = ConceptIdentifier.of("First-Id")
    private val secondId = ConceptIdentifier.of("Second-Id")

    @Test
    fun `passing of null value arguments in builder method does fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = TestSchema::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, TestBuilder::class) { dataCollector ->
                dataCollector.newTestConcept(firstId,"Foo")
                assertThrows(IllegalArgumentException::class.java) {
                    dataCollector.newTestConcept(secondId,null)
                }
            }
        }
    }

    @Test
    fun `passing of null value as concept identifier in builder method does fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = TestSchema::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, TestBuilder::class) { dataCollector ->
                assertThrows(IllegalArgumentException::class.java) {
                    dataCollector.newTestConcept(null,"Foo")
                }
            }
        }
    }

    @Test
    fun `test insertion of nullable value in builder method does not fail but ignore value`() {
        val schemaInstance: TestSchema = SchemaApi.withSchema(schemaDefinitionClass = TestSchema::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, TestBuilder::class) { dataCollector ->
                dataCollector.newTestConceptIgnoringNullValues(firstId, "Foo")
                dataCollector.newTestConceptIgnoringNullValues(secondId, null)

                dataCollector.newTestConceptWithSubDsl("Fez") {
                    this.changeNullableValue(null)
                    this.changeNullableValue("Fraz")
                    this.changeNullableValue(null)
                }
            }
        }

        val testConceptList = schemaInstance.getTestConcepts()
        Assertions.assertEquals(3, testConceptList.size)

        val testConceptWithNonNullValue = testConceptList[0]
        Assertions.assertEquals("Foo", testConceptWithNonNullValue.getNullableValue())

        val testConceptWithNullValue = testConceptList[1]
        Assertions.assertNull(testConceptWithNullValue.getNullableValue())

        val testConceptWithSecondNonNullValue = testConceptList[2]
        Assertions.assertEquals("Fraz", testConceptWithSecondNonNullValue.getNullableValue())
    }


    @Test
    fun `test passing null for a sub-builder does fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = TestSchema::class) { schemaContext ->
            assertThrows(IllegalStateException::class.java) {
                BuilderApi.withBuilder(schemaContext, TestBuilder::class) { dataCollector ->
                    dataCollector.newTestConceptWithSubDsl("Fez", null)
                }
            }
        }
    }
}