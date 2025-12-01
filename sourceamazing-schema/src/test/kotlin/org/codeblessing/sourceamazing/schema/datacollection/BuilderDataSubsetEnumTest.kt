package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED")
class BuilderDataSubsetEnumTest {
    enum class AllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    private interface MyConcepts {

        interface MyConcept {

            val enumFacetValue: AllDatatypesEnum
        }

        val concept: MyConcept
    }

    @Test
    fun `test using the enum type defined on the facet to set the enum value should not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAllDatatypesEnum::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.doSetEnumValue(AllDatatypesEnum.INT)
                    }
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }

    enum class CompatibleNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
    }

    @Test
    fun `test using a enum type not defined on the facet but with subset of all enum values to set the enum value should not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithCompatibleNumericDatatypesEnum::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.doSetEnumValue(CompatibleNumericDatatypesEnum.INT)
                    }
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }

    enum class ExactCopyOfAllDatatypesEnum {
        STRING,
        INT,
        FLOAT,
        DOUBLE,
        UUID,
    }

    @Test
    fun `test using a enum type not defined on the facet but with exactly equal enum values to set the enum value should not fail`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithExactCopyOfAllDatatypesEnum::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.doSetEnumValue(ExactCopyOfAllDatatypesEnum.INT)
                    }
                }
            }

        Assertions.assertEquals(AllDatatypesEnum.INT, schemaInstance.concept.enumFacetValue)
    }

    enum class IncompatibleWithNumericDatatypesEnum {
        INT,
        FLOAT,
        DOUBLE,
        BYTE, // this is an incompatible facet value
    }

    @Test
    fun `test using a enum type not defined on the facet but with a incompatible subset of enum values to set the enum value should throw an exception`() {
        assertThrows<BuilderMethodSyntaxException> {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) { conceptNameAndIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithIncompatibleWithAllDatatypesEnum::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.doSetEnumValue(IncompatibleWithNumericDatatypesEnum.INT)
                    }
                }
            }
        }
    }
}
