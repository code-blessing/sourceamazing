package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataSubsetEnumTest {
    enum class AllDatatypesEnum {
        @Suppress("UNUSED")
        STRING,

        @Suppress("UNUSED")
        INT,

        @Suppress("UNUSED")
        FLOAT,

        @Suppress("UNUSED")
        DOUBLE,

        @Suppress("UNUSED")
        UUID,
    }

    private interface SchemaWithConceptWithEnumerationFacet {

        interface ConceptWithEnumerationFacet {

            @Suppress("UNUSED")
            @Facet
            val enumFacetValue: AllDatatypesEnum
        }

        @Suppress("UNUSED")
        @Facet
        val concept: ConceptWithEnumerationFacet
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAllDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumerationFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concept", referencedConceptAlias = "myConcept")
        fun doSetEnumValue(
            @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "enumFacetValue")
            enumValue: AllDatatypesEnum,
        )
    }

    @Test
    fun `test using the enum type defined on the facet to set the enum value should not fail`() {
        val schemaInstance: SchemaWithConceptWithEnumerationFacet =
            SchemaApi.withSchema(SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithEnumerationFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAllDatatypesEnum::class,
                    ) { builder ->
                        builder.doSetEnumValue(AllDatatypesEnum.INT)
                    }
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }

    enum class CompatibleNumericDatatypesEnum {
        @Suppress("UNUSED")
        INT,

        @Suppress("UNUSED")
        FLOAT,

        @Suppress("UNUSED")
        DOUBLE,
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithCompatibleNumericDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumerationFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concept", referencedConceptAlias = "myConcept")
        fun doSetEnumValue(
            @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "enumFacetValue")
            enumValue: CompatibleNumericDatatypesEnum,
        )
    }

    @Test
    fun `test using a enum type not defined on the facet but with subset of all enum values to set the enum value should not fail`() {
        val schemaInstance: SchemaWithConceptWithEnumerationFacet =
            SchemaApi.withSchema(SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithEnumerationFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithCompatibleNumericDatatypesEnum::class,
                    ) { builder ->
                        builder.doSetEnumValue(CompatibleNumericDatatypesEnum.INT)
                    }
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)

    }

    enum class ExactCopyOfAllDatatypesEnum {
        @Suppress("UNUSED")
        STRING,

        @Suppress("UNUSED")
        INT,

        @Suppress("UNUSED")
        FLOAT,

        @Suppress("UNUSED")
        DOUBLE,

        @Suppress("UNUSED")
        UUID,
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithExactCopyOfAllDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumerationFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concept", referencedConceptAlias = "myConcept")
        fun doSetEnumValue(
            @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "enumFacetValue")
            enumValue: ExactCopyOfAllDatatypesEnum,
        )
    }

    @Test
    fun `test using a enum type not defined on the facet but with exactly equal enum values to set the enum value should not fail`() {
        val schemaInstance: SchemaWithConceptWithEnumerationFacet =
            SchemaApi.withSchema(SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithEnumerationFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithExactCopyOfAllDatatypesEnum::class,
                    ) { builder ->
                        builder.doSetEnumValue(ExactCopyOfAllDatatypesEnum.INT)
                    }
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }


    enum class IncompatibleWithNumericDatatypesEnum {
        @Suppress("UNUSED")
        INT,

        @Suppress("UNUSED")
        FLOAT,

        @Suppress("UNUSED")
        DOUBLE,

        @Suppress("UNUSED")
        BYTE, // this is an incompatible facet value
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithIncompatibleWithAllDatatypesEnum {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithEnumerationFacet.ConceptWithEnumerationFacet::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concept", referencedConceptAlias = "myConcept")
        fun doSetEnumValue(
            @SetFacetValue(conceptToModifyAlias = "myConcept", facetToModify = "enumFacetValue")
            enumValue: IncompatibleWithNumericDatatypesEnum,
        )
    }

    @Test
    fun `test using a enum type not defined on the facet but with a incompatible subset of enum values to set the enum value should throw an exception`() {
        assertThrows<BuilderMethodSyntaxException> {
            SchemaApi.withSchema(SchemaWithConceptWithEnumerationFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithEnumerationFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithIncompatibleWithAllDatatypesEnum::class,
                    ) { builder ->
                        builder.doSetEnumValue(IncompatibleWithNumericDatatypesEnum.INT)
                    }
                }
            }
        }
    }
}
