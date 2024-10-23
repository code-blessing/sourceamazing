package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataSubsetEnumTest {
    enum class AllDatatypesEnum {
        @Suppress("UNUSED") STRING,
        @Suppress("UNUSED") INT,
        @Suppress("UNUSED") FLOAT,
        @Suppress("UNUSED") DOUBLE,
        @Suppress("UNUSED") UUID,
    }

    @Schema(concepts = [SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets::class])
    private interface SchemaWithConceptWithEnumerationFacet {

        @Concept(facets = [
            ConceptWithEnumFacets.AllDatatypesEnumerationFacet::class,
        ])
        interface ConceptWithEnumFacets {
            @EnumFacet(AllDatatypesEnum::class)
            interface AllDatatypesEnumerationFacet

            @QueryFacetValue(AllDatatypesEnumerationFacet::class)
            fun getEnumFacetValue(): AllDatatypesEnum
        }

        @QueryConcepts([ConceptWithEnumFacets::class])
        fun getConcepts(): List<ConceptWithEnumFacets>
    }

    @Builder
    private interface BuilderMethodWithAllDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSetEnumValue(
            @SetFacetValue(facetToModify = SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets.AllDatatypesEnumerationFacet::class)
            enumValue: AllDatatypesEnum,
        )
    }

    @Test
    fun `test using the enum type defined on the facet to set the enum value should not fail`() {
        val schemaInstance: SchemaWithConceptWithEnumerationFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithAllDatatypesEnum::class) { builder ->
                builder.doSetEnumValue(AllDatatypesEnum.INT)
            }
        }

        val concept = schemaInstance.getConcepts().first()
        Assertions.assertEquals(AllDatatypesEnum.INT, concept.getEnumFacetValue())
    }

    enum class CompatibleNumericDatatypesEnum {
        @Suppress("UNUSED") INT,
        @Suppress("UNUSED") FLOAT,
        @Suppress("UNUSED") DOUBLE,
    }

    @Builder
    private interface BuilderMethodWithCompatibleNumericDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSetEnumValue(
            @SetFacetValue(facetToModify = SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets.AllDatatypesEnumerationFacet::class)
            enumValue: CompatibleNumericDatatypesEnum,
        )
    }

    @Test
    fun `test using a enum type not defined on the facet but with subset of all enum values to set the enum value should not fail`() {
        val schemaInstance: SchemaWithConceptWithEnumerationFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCompatibleNumericDatatypesEnum::class) { builder ->
                builder.doSetEnumValue(CompatibleNumericDatatypesEnum.INT)
            }
        }

        val concept = schemaInstance.getConcepts().first()
        Assertions.assertEquals(AllDatatypesEnum.INT, concept.getEnumFacetValue())

    }

    enum class ExactCopyOfAllDatatypesEnum {
        @Suppress("UNUSED") STRING,
        @Suppress("UNUSED") INT,
        @Suppress("UNUSED") FLOAT,
        @Suppress("UNUSED") DOUBLE,
        @Suppress("UNUSED") UUID,
    }

    @Builder
    private interface BuilderMethodWithExactCopyOfAllDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSetEnumValue(
            @SetFacetValue(facetToModify = SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets.AllDatatypesEnumerationFacet::class)
            enumValue: ExactCopyOfAllDatatypesEnum,
        )
    }

    @Test
    fun `test using a enum type not defined on the facet but with exactly equal enum values to set the enum value should not fail`() {
        val schemaInstance: SchemaWithConceptWithEnumerationFacet = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithExactCopyOfAllDatatypesEnum::class) { builder ->
                builder.doSetEnumValue(ExactCopyOfAllDatatypesEnum.INT)
            }
        }

        val concept = schemaInstance.getConcepts().first()
        Assertions.assertEquals(AllDatatypesEnum.INT, concept.getEnumFacetValue())

    }


    enum class IncompatibleWithNumericDatatypesEnum {
        @Suppress("UNUSED") INT,
        @Suppress("UNUSED") FLOAT,
        @Suppress("UNUSED") DOUBLE,
        @Suppress("UNUSED") BYTE, // this is an incompatible facet value
    }

    @Builder
    private interface BuilderMethodWithIncompatibleWithAllDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets::class)
        @SetRandomConceptIdentifierValue
        fun doSetEnumValue(
            @SetFacetValue(facetToModify = SchemaWithConceptWithEnumerationFacet.ConceptWithEnumFacets.AllDatatypesEnumerationFacet::class)
            enumValue: IncompatibleWithNumericDatatypesEnum,
        )
    }

    @Test
    fun `test using a enum type not defined on the facet but with a incompatible subset of enum values to set the enum value should throw an exception`() {
        assertThrows<BuilderMethodParameterSyntaxException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithIncompatibleWithAllDatatypesEnum::class
                ) { builder ->
                    builder.doSetEnumValue(IncompatibleWithNumericDatatypesEnum.INT)
                }
            }
        }
    }
}