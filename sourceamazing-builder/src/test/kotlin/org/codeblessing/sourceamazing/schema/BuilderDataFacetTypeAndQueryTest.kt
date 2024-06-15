package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.BuilderDataFacetTypeAndQueryTest.SchemaWithConceptWithFacet.MyEnum
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataFacetTypeAndQueryTest {

    @Schema(concepts = [SchemaWithConceptWithFacet.ConceptWithFacet::class])
    private interface SchemaWithConceptWithFacet {

        enum class MyEnum {
            FOO,
            BAR,
        }

        @Concept(facets = [
            ConceptWithFacet.TextFacet::class,
            ConceptWithFacet.BoolFacet::class,
            ConceptWithFacet.NumberFacet::class,
            ConceptWithFacet.EnumerationFacet::class,
        ])
        interface ConceptWithFacet {
            @StringFacet(minimumOccurrences = 0, maximumOccurrences = 3)
            interface TextFacet

            @BooleanFacet(minimumOccurrences = 0, maximumOccurrences = 3)
            interface BoolFacet

            @IntFacet(minimumOccurrences = 0, maximumOccurrences = 3)
            interface NumberFacet

            @EnumFacet(minimumOccurrences = 0, maximumOccurrences = 3, enumerationClass = MyEnum::class)
            interface EnumerationFacet

            @QueryFacetValue(TextFacet::class)
            fun getTextFacetAsList(): List<String>

            @QueryFacetValue(TextFacet::class)
            fun getTextFacet(): String

            @QueryFacetValue(TextFacet::class)
            fun getTextFacetNullable(): String?

            @QueryFacetValue(TextFacet::class)
            fun getTextFacetAsNullableAny(): Any?

            @QueryFacetValue(BoolFacet::class)
            fun getBoolFacetAsList(): List<Boolean>

            @QueryFacetValue(BoolFacet::class)
            fun getBoolFacet(): Boolean

            @QueryFacetValue(BoolFacet::class)
            fun getBoolFacetNullable(): Boolean?

            @QueryFacetValue(BoolFacet::class)
            fun getBoolFacetAsNullableAny(): Any?

            @QueryFacetValue(NumberFacet::class)
            fun getNumberFacetAsList(): List<Int>

            @QueryFacetValue(NumberFacet::class)
            fun getNumberFacet(): Int

            @QueryFacetValue(NumberFacet::class)
            fun getNumberFacetNullable(): Int?

            @QueryFacetValue(NumberFacet::class)
            fun getNumberFacetAsNullableAny(): Any?

            @QueryFacetValue(EnumerationFacet::class)
            fun getEnumerationFacetAsList(): List<MyEnum>

            @QueryFacetValue(EnumerationFacet::class)
            fun getEnumerationFacet(): MyEnum

            @QueryFacetValue(EnumerationFacet::class)
            fun getEnumerationFacetNullable(): MyEnum?

            @QueryFacetValue(EnumerationFacet::class)
            fun getEnumerationFacetAsNullableAny(): Any?

        }

        @QueryConcepts(conceptClasses = [ConceptWithFacet::class])
        fun getConcepts(): List<ConceptWithFacet>
    }


    @Builder
    private interface BuilderToAddOrReplaceFacets {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        fun createConcept(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "myConcept")
        interface NestedBuilder {
            @BuilderMethod
            fun addFacetValues(
                @SetFacetValue(
                    facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class,
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myTextValue: String,
                @SetFacetValue(
                    facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.BoolFacet::class,
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myBoolValue: Boolean,
                @SetFacetValue(
                    facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class,
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myNumberValue: Int,
                @SetFacetValue(
                    facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.EnumerationFacet::class,
                    conceptToModifyAlias = "myConcept",
                    facetModificationRule = FacetModificationRule.ADD,
                )
                myEnumValue: MyEnum,
            ): NestedBuilder
        }
    }

    @Test
    fun `test insert zero values for all the different types of facets does not fail`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceFacets::class) { builder ->
                builder.createConcept()
                    // no facet values added
            }
        }

        val concept = schemaInstance.getConcepts().first()
        Assertions.assertEquals(0, concept.getTextFacetAsList().size)
        Assertions.assertNull(concept.getTextFacetNullable())
        Assertions.assertNull(concept.getTextFacetAsNullableAny())
        assertThrows<IllegalStateException> {
            concept.getTextFacet()
        }

        Assertions.assertEquals(0, concept.getBoolFacetAsList().size)
        Assertions.assertNull(concept.getBoolFacetNullable())
        Assertions.assertNull(concept.getBoolFacetAsNullableAny())
        assertThrows<IllegalStateException> {
            concept.getBoolFacet()
        }

        Assertions.assertEquals(0, concept.getNumberFacetAsList().size)
        Assertions.assertNull(concept.getNumberFacetNullable())
        Assertions.assertNull(concept.getNumberFacetAsNullableAny())
        assertThrows<IllegalStateException> {
            concept.getNumberFacet()
        }

        Assertions.assertEquals(0, concept.getEnumerationFacetAsList().size)
        Assertions.assertNull(concept.getEnumerationFacetNullable())
        Assertions.assertNull(concept.getEnumerationFacetAsNullableAny())
        assertThrows<IllegalStateException> {
            concept.getEnumerationFacet()
        }

    }

    @Test
    fun `test insert exactly one value for all the different types of facets does not fail and return null values`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceFacets::class) { builder ->
                builder.createConcept()
                    .addFacetValues(myTextValue = "hallo", myBoolValue = true, myNumberValue = 42, myEnumValue = MyEnum.FOO)
            }
        }

        val concept = schemaInstance.getConcepts().first()
        Assertions.assertEquals(1, concept.getTextFacetAsList().size)
        Assertions.assertEquals("hallo", concept.getTextFacetAsList()[0])
        Assertions.assertEquals("hallo", concept.getTextFacetNullable())
        Assertions.assertEquals("hallo", concept.getTextFacetAsNullableAny())
        Assertions.assertEquals("hallo", concept.getTextFacet())

        Assertions.assertEquals(1, concept.getBoolFacetAsList().size)
        Assertions.assertEquals(true, concept.getBoolFacetAsList()[0])
        Assertions.assertEquals(true, concept.getBoolFacetNullable())
        Assertions.assertEquals(true, concept.getBoolFacetAsNullableAny())
        Assertions.assertEquals(true, concept.getBoolFacet())

        Assertions.assertEquals(1, concept.getNumberFacetAsList().size)
        Assertions.assertEquals(42, concept.getNumberFacetAsList()[0])
        Assertions.assertEquals(42, concept.getNumberFacetNullable())
        Assertions.assertEquals(42, concept.getNumberFacetAsNullableAny())
        Assertions.assertEquals(42, concept.getNumberFacet())

        Assertions.assertEquals(1, concept.getEnumerationFacetAsList().size)
        Assertions.assertEquals(MyEnum.FOO, concept.getEnumerationFacetAsList()[0])
        Assertions.assertEquals(MyEnum.FOO, concept.getEnumerationFacetNullable())
        Assertions.assertEquals(MyEnum.FOO, concept.getEnumerationFacetAsNullableAny())
        Assertions.assertEquals(MyEnum.FOO, concept.getEnumerationFacet())

    }

    @Test
    fun `test insert two values for all the different types of facets does not fail`() {
        val schemaInstance: SchemaWithConceptWithFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddOrReplaceFacets::class) { builder ->
                builder.createConcept()
                    .addFacetValues(myTextValue = "hallo1", myBoolValue = false, myNumberValue = 43, myEnumValue = MyEnum.BAR)
                    .addFacetValues(myTextValue = "hallo2", myBoolValue = true, myNumberValue = 44, myEnumValue = MyEnum.FOO)
            }
        }

        val concept = schemaInstance.getConcepts().first()
        Assertions.assertEquals(2, concept.getTextFacetAsList().size)
        Assertions.assertEquals("hallo1", concept.getTextFacetAsList()[0])
        Assertions.assertEquals("hallo2", concept.getTextFacetAsList()[1])
        Assertions.assertEquals("hallo1", concept.getTextFacetNullable())
        Assertions.assertEquals("hallo1", concept.getTextFacetAsNullableAny())
        Assertions.assertEquals("hallo1", concept.getTextFacet())

        Assertions.assertEquals(2, concept.getBoolFacetAsList().size)
        Assertions.assertEquals(false, concept.getBoolFacetAsList()[0])
        Assertions.assertEquals(true, concept.getBoolFacetAsList()[1])
        Assertions.assertEquals(false, concept.getBoolFacetNullable())
        Assertions.assertEquals(false, concept.getBoolFacetAsNullableAny())
        Assertions.assertEquals(false, concept.getBoolFacet())

        Assertions.assertEquals(2, concept.getNumberFacetAsList().size)
        Assertions.assertEquals(43, concept.getNumberFacetAsList()[0])
        Assertions.assertEquals(44, concept.getNumberFacetAsList()[1])
        Assertions.assertEquals(43, concept.getNumberFacetNullable())
        Assertions.assertEquals(43, concept.getNumberFacetAsNullableAny())
        Assertions.assertEquals(43, concept.getNumberFacet())

        Assertions.assertEquals(2, concept.getEnumerationFacetAsList().size)
        Assertions.assertEquals(MyEnum.BAR, concept.getEnumerationFacetAsList()[0])
        Assertions.assertEquals(MyEnum.FOO, concept.getEnumerationFacetAsList()[1])
        Assertions.assertEquals(MyEnum.BAR, concept.getEnumerationFacetNullable())
        Assertions.assertEquals(MyEnum.BAR, concept.getEnumerationFacetAsNullableAny())
        Assertions.assertEquals(MyEnum.BAR, concept.getEnumerationFacet())
    }

}