package org.codeblessing.sourceamazing.builder.factories

import java.io.File
import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.api.*
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewClazzModel
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsValue
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

@Suppress("UNUSED", "UNUSED_PARAMETER")
class BuilderFactoriesStructureTest {

    private interface MyClazzes {

        interface MyClazz {
            val texts: List<String>

            val numbers: List<Int>
        }

        val clazzes: List<MyClazz>
    }

    @Builder private interface MyBuilder

    private interface MyBuilderImplAsInterface : MyBuilder

    @Test
    fun `test that an interface as implementation class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_IMPLEMENTATION_MUST_NOT_BE_ABSTRACT
        ) {
            createBuilderWithFactory(MyBuilder::class, setOf(MyBuilder::class by MyBuilderImplAsInterface::class))
        }
    }

    private abstract class MyBuilderImplAsAbstractClass : MyBuilder

    @Test
    fun `test that an abstract class as implementation class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_IMPLEMENTATION_MUST_NOT_BE_ABSTRACT
        ) {
            createBuilderWithFactory(MyBuilder::class, setOf(MyBuilder::class by MyBuilderImplAsAbstractClass::class))
        }
    }

    private class MyBuilderImplWithTypeParameters<T> : MyBuilder

    @Test
    fun `test that an implementation class with type parameters should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_IMPLEMENTATION_MUST_HAVE_A_VALID_CONSTRUCTOR
        ) {
            createBuilderWithFactory(
                MyBuilder::class,
                setOf(MyBuilder::class by MyBuilderImplWithTypeParameters::class),
            )
        }
    }

    private class MyBuilderImplWithInvalidTypeInConstructor(myString: String?) : MyBuilder

    @Test
    fun `test that an implementation class with wrong constructor parameters should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_IMPLEMENTATION_MUST_HAVE_A_VALID_CONSTRUCTOR
        ) {
            createBuilderWithFactory(
                MyBuilder::class,
                setOf(MyBuilder::class by MyBuilderImplWithInvalidTypeInConstructor::class),
            )
        }
    }

    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private class MyBuilderImplWithBuilderAnnotations : MyBuilder

    @Test
    fun `test that an implementation class having builder annotations on the class should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_IMPLEMENTATION_CAN_NOT_HAVE_BUILDER_ANNOTATIONS
        ) {
            createBuilderWithFactory(
                MyBuilder::class,
                setOf(MyBuilder::class by MyBuilderImplWithBuilderAnnotations::class),
            )
        }
    }

    private class MyBuilderImplWithBuilderAnnotationsOnMethods : MyBuilder {
        @NewClazzModel(MyClazzes.MyClazz::class, "myClazz")
        fun doSomething() {
            // do nothing
        }
    }

    @Test
    fun `test that an implementation class having builder annotations on the methods should not throw an exception`() {
        // we cannot check these annotations, as otherwise, the kotlin (https://kotlinlang.org/docs/delegation.html)
        // would not be possible
        createBuilderWithFactory(
            MyBuilder::class,
            setOf(MyBuilder::class by MyBuilderImplWithBuilderAnnotationsOnMethods::class),
        )
    }

    private class MyBuilderImplWithBuilderAnnotationsOnMethodParameters : MyBuilder {
        fun doSomething(@SetAsValue("myClazz", "texts") myString: String) {
            // do nothing
        }
    }

    @Test
    fun `test that an implementation class having builder annotations on the method parameters should not throw an exception`() {
        // we cannot check these annotations, as otherwise, the kotlin (https://kotlinlang.org/docs/delegation.html)
        // would not be possible
        createBuilderWithFactory(
            MyBuilder::class,
            setOf(MyBuilder::class by MyBuilderImplWithBuilderAnnotationsOnMethodParameters::class),
        )
    }

    private class MyCorrectBuilderImpl(
        optionalFileParameter: File = File("."),
        builder: MyBuilder,
        schemaContext: SchemaContext,
        builderContext: BuilderContext,
        optionalIntParameter: Int = 42,
        alternativeBuilder: MyBuilder,
        alternativeSchemaContext: SchemaContext,
        alternativeBuilderContext: BuilderContext,
        optionalStringParameter: String = "foo",
        nullableStringParameter: String? = null,
        nullableIntParameter: Int? = null,
    ) : MyBuilder

    @Test
    fun `test that a correct implementation class should return without exceptions`() {
        createBuilderWithFactory(MyBuilder::class, setOf(MyBuilder::class by MyCorrectBuilderImpl::class))
    }

    @Builder
    private interface MyBuilderWithNonBuilderMethods {

        fun doSomething()
    }

    @Test
    fun `test that a builder interface with non-builder methods without providing an implementation should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.BUILDER_WITH_NON_BUILDER_METHODS_MUST_HAVE_BUILDER_IMPLEMENTATION
        ) {
            createBuilderWithFactory(MyBuilderWithNonBuilderMethods::class, setOf())
        }
    }

    @Builder
    private interface MyBuilderWithBuilderAnnotationsOnNonBuilderMethods {
        @NewClazzModel(MyClazzes.MyClazz::class, "myClazz") fun doSomething()
    }

    private class MyBuilderWithBuilderAnnotationsOnNonBuilderMethodsImpl :
        MyBuilderWithBuilderAnnotationsOnNonBuilderMethods {
        override fun doSomething() {
            // do nothing
        }
    }

    @Test
    fun `test that a builder interface with non-builder methods that have builder annotations should throw an exception`() {
        assertExceptionWithErrorCode<BuilderSyntaxException>(
            BuilderErrorCode.NON_BUILDER_METHODS_CAN_NOT_HAVE_BUILDER_ANNOTATIONS
        ) {
            createBuilderWithFactory(
                MyBuilderWithBuilderAnnotationsOnNonBuilderMethods::class,
                setOf(
                    MyBuilderWithBuilderAnnotationsOnNonBuilderMethods::class by
                        MyBuilderWithBuilderAnnotationsOnNonBuilderMethodsImpl::class
                ),
            )
        }
    }

    private fun <B : Any, I : B> createBuilderWithFactory(
        builderClass: KClass<B>,
        builderFactories: Set<BuilderFactory<*, *>>,
    ) {
        SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, builderClass, builderFactories) {
                // nothing to do
            }
        }
    }
}
