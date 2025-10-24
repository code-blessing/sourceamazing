package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode.CLASS_MUST_BE_AN_INTERFACE
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiBuilderAnnotationTest {

    private interface MyClazzes {

        interface MyClazz {
            val text: String
        }

        val clazzes: List<MyClazz>
    }

    @Builder private interface EmptyBuilder

    @Test
    fun `test create an empty builder should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, EmptyBuilder::class) {
                // do nothing
            }
        }
    }

    @Builder private sealed interface SealedEmptyBuilder

    @Test
    fun `test sealed interface instead of interface builder class should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, SealedEmptyBuilder::class) {
                // do nothing
            }
        }
    }

    private interface ParentInterface

    @Builder private interface BuilderWithParentInterface : ParentInterface

    @Test
    fun `test builder with parent interface without annotations should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderWithParentInterface::class) {
                // do nothing
            }
        }
    }

    interface UnannotatedBuilder

    @Test
    fun `test unannotated builder class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.MUST_HAVE_ANNOTATION) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, UnannotatedBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private abstract class AbstractClassInsteadOfInterfaceBuilder

    @Test
    fun `test abstract class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, AbstractClassInsteadOfInterfaceBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private class ClassInsteadOfInterfaceSchema

    @Test
    fun `test class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, ClassInsteadOfInterfaceSchema::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private enum class EnumInsteadOfInterfaceBuilder

    @Test
    fun `test enum class instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, EnumInsteadOfInterfaceBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private object ObjectInsteadOfInterfaceBuilder

    @Test
    fun `test object instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, ObjectInsteadOfInterfaceBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private annotation class AnnotationInsteadOfInterfaceBuilder

    @Test
    fun `test annotation instead of interface builder class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, AnnotationInsteadOfInterfaceBuilder::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "allMyClazzes")
    private interface BuilderWithExpectedAliasFromSuperiorBuilderAnnotation

    @Test
    fun `test top level builder with ExpectedAliasFromSuperiorBuilder should not throw an exception`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            val clazzData = schemaContext.dataCollector.rootClazzModel()
            BuilderApi.withBuilder(
                schemaContext,
                BuilderWithExpectedAliasFromSuperiorBuilderAnnotation::class,
                mapOf("allMyClazzes" to clazzData.clazzAndModelId),
            ) {
                // do nothing
            }
        }
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderWithExpectedDefaultRootAliasFromSuperiorBuilderAnnotation

    @Test
    fun `test top level builder with default root element in ExpectedAliasFromSuperiorBuilder should not throw an exception`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderWithExpectedDefaultRootAliasFromSuperiorBuilderAnnotation::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation {
        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes.MyClazz::class, alias = "foo")
        private interface NestedBuilder

        @BuilderMethod @NewClazzModel(MyClazzes.MyClazz::class, alias = "foo") fun doSomething(): NestedBuilder
    }

    @Test
    fun `test nested builder with ExpectedAliasFromSuperiorBuilder should not fail`() {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderWithNestedBuilderHavingExpectedAliasFromSuperiorBuilderAnnotation::class,
            ) {
                // do nothing
            }
        }
    }

    @Builder private interface ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses

    @Builder
    private interface BuilderWithTwoBuilderAnnotationsInHierarchyClasses :
        ParentBuilderWithTwoBuilderAnnotationsInHierarchyClasses

    @Test
    fun `test builder with two builder annotations in hierarchy should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithTwoBuilderAnnotationsInHierarchyClasses::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder private interface BuilderWithGenericTypeParameter<T>

    @Test
    fun `test builder class with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.NO_GENERIC_TYPE_PARAMETER) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithGenericTypeParameter::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithProperties {
        val builderMethodProperty: String
    }

    @Test
    fun `test builder class with properties should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.CLASS_CANNOT_HAVE_PROPERTIES) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithProperties::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithExtensionType {
        interface MyExtensionType

        @BuilderMethod @NewClazzModel(MyClazzes.MyClazz::class, "foo") fun MyExtensionType.doSomething()
    }

    @Test
    fun `test builder method with extension type should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(BuilderErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithExtensionType::class) {
                    // do nothing
                }
            }
        }
    }
}
