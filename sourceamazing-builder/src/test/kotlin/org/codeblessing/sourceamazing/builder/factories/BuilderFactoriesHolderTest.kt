package org.codeblessing.sourceamazing.builder.factories

import org.codeblessing.sourceamazing.builder.alias.TypeSafeBuilderContextImpl
import org.codeblessing.sourceamazing.builder.api.by
import org.codeblessing.sourceamazing.builder.api.create
import org.codeblessing.sourceamazing.builder.typesafeapi.TypeSafeBuilderContext
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuilderFactoriesHolderTest {

    private interface OneBuilderClass

    private class OneBuilderClassImpl : OneBuilderClass

    @Suppress("UNUSED")
    private class OneBuilderClassImplOptionalParams(val myString: String = "String", val myBoolean: Boolean = true) :
        OneBuilderClass

    @Suppress("UNUSED")
    private class OneBuilderClassImplSchemaContext(
        val myString: String = "String",
        val mySchemaContext: SchemaContext,
        val mySecondSchemaContext: SchemaContext,
        val myBoolean: Boolean = true,
    ) : OneBuilderClass

    @Nested
    inner class HasImplementationTests {
        @Test
        fun `test that false is returned if no implementation is available`() {
            val builderFactoriesHolder = BuilderFactoriesHolder(emptySet())

            assertEquals(false, builderFactoriesHolder.hasImplementation(OneBuilderClass::class))
        }

        @Test
        fun `test that true is returned if an implementation is available`() {
            val builderFactoriesHolder =
                BuilderFactoriesHolder(setOf(OneBuilderClass::class by OneBuilderClassImpl::class))

            assertEquals(true, builderFactoriesHolder.hasImplementation(OneBuilderClass::class))
        }
    }

    @Nested
    inner class GetFinalClassTests {

        @Test
        fun `test that the given interface is returned if no implementation is available`() {
            val builderFactoriesHolder = BuilderFactoriesHolder(emptySet())

            assertEquals(OneBuilderClass::class, builderFactoriesHolder.getFinalClass(OneBuilderClass::class))
        }

        @Test
        fun `test that the implementation class is returned if available`() {
            val builderFactoriesHolder =
                BuilderFactoriesHolder(setOf(OneBuilderClass::class by OneBuilderClassImpl::class))

            assertEquals(OneBuilderClassImpl::class, builderFactoriesHolder.getFinalClass(OneBuilderClass::class))
        }
    }

    @Nested
    inner class CreateImplementationTests {

        val noopSchemaContext: TypeSafeSchemaContext = NoopTypeSafeSchemaContext()
        val noopBuilderContext: TypeSafeBuilderContext = TypeSafeBuilderContextImpl(emptyMap())

        inner class MockProxyOneBuilderClass : OneBuilderClass

        @Test
        fun `test that the given builder instance is returned if no implementation available`() {
            val builderFactoriesHolder = BuilderFactoriesHolder(emptySet())
            val implementationInstance =
                builderFactoriesHolder.createImplementation(
                    OneBuilderClass::class,
                    MockProxyOneBuilderClass(),
                    noopSchemaContext,
                    noopBuilderContext,
                )

            assertEquals(MockProxyOneBuilderClass::class, implementationInstance::class)
        }

        @Test
        fun `test that the implementation instance is created and is returned if available`() {
            val builderFactoriesHolder =
                BuilderFactoriesHolder(setOf(OneBuilderClass::class by OneBuilderClassImpl::class))
            val implementationInstance =
                builderFactoriesHolder.createImplementation(
                    OneBuilderClass::class,
                    MockProxyOneBuilderClass(),
                    noopSchemaContext,
                    noopBuilderContext,
                )

            assertEquals(OneBuilderClassImpl::class, implementationInstance::class)
            assertTrue(OneBuilderClassImpl::class.isInstance(implementationInstance))
        }

        @Test
        fun `test that the implementation instance with optional constructor parameter is created`() {
            val builderFactoriesHolder =
                BuilderFactoriesHolder(setOf(OneBuilderClass::class by OneBuilderClassImplOptionalParams::class))
            val implementationInstance =
                builderFactoriesHolder.createImplementation(
                    OneBuilderClass::class,
                    MockProxyOneBuilderClass(),
                    noopSchemaContext,
                    noopBuilderContext,
                )

            assertEquals(OneBuilderClassImplOptionalParams::class, implementationInstance::class)
            assertTrue(OneBuilderClassImplOptionalParams::class.isInstance(implementationInstance))
        }

        @Test
        fun `test that the implementation instance with SchemaContext constructor parameter is created`() {
            val builderFactoriesHolder =
                BuilderFactoriesHolder(setOf(OneBuilderClass::class by OneBuilderClassImplSchemaContext::class))
            val implementationInstance =
                builderFactoriesHolder.createImplementation(
                    OneBuilderClass::class,
                    MockProxyOneBuilderClass(),
                    noopSchemaContext,
                    noopBuilderContext,
                )

            assertEquals(OneBuilderClassImplSchemaContext::class, implementationInstance::class)
            assertTrue(OneBuilderClassImplSchemaContext::class.isInstance(implementationInstance))
            assertEquals("String", (implementationInstance as OneBuilderClassImplSchemaContext).myString)
        }

        @Test
        fun `test that the implementation instance is created by the supplier method`() {
            val builderFactoriesHolder =
                BuilderFactoriesHolder(
                    setOf(
                        OneBuilderClass::class.create(OneBuilderClassImplSchemaContext::class) { _, schemaContext, _ ->
                            OneBuilderClassImplSchemaContext(
                                "Foo",
                                mySchemaContext = schemaContext,
                                mySecondSchemaContext = schemaContext,
                                myBoolean = false,
                            )
                        }
                    )
                )
            val implementationInstance =
                builderFactoriesHolder.createImplementation(
                    OneBuilderClass::class,
                    MockProxyOneBuilderClass(),
                    noopSchemaContext,
                    noopBuilderContext,
                )

            assertEquals(OneBuilderClassImplSchemaContext::class, implementationInstance::class)
            assertTrue(OneBuilderClassImplSchemaContext::class.isInstance(implementationInstance))
            assertEquals("Foo", (implementationInstance as OneBuilderClassImplSchemaContext).myString)
        }
    }
}
