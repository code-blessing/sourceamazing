package org.codeblessing.sourceamazing.schema.utils.type

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KFunctionUtilTest {

    interface Foo {
        fun bar(): String
    }

    @Test
    fun `returns base function if passed class is the base class itself`() {
        assertEquals(Foo::bar, KFunctionUtil.functionOrDerivedFunction(Foo::bar, Foo::class))
    }

    @Suppress("UNUSED")
    interface Bar {
        fun bar(): String
    }

    @Test
    fun `returns base function if passed class is not in hierarchy with the base class`() {
        assertEquals(Foo::bar, KFunctionUtil.functionOrDerivedFunction(Foo::bar, Bar::class))
    }

    @Suppress("UNUSED")
    interface FooDerived : Foo {
        override fun bar(): String
    }

    @Test
    fun `returns derived function if passed class is in hierarchy with the base class`() {
        assertEquals(FooDerived::bar, KFunctionUtil.functionOrDerivedFunction(Foo::bar, FooDerived::class))
    }

    @Suppress("UNUSED")
    interface FooDerivedButNotOverridden : Foo {
        fun otherBar(): String
    }

    @Test
    fun `returns derived function if passed class is in hierarchy with the base class and function is not overridden`() {
        assertEquals(
            FooDerivedButNotOverridden::bar,
            KFunctionUtil.functionOrDerivedFunction(Foo::bar, FooDerivedButNotOverridden::class),
        )
    }

    interface FooWithGenericParameter<T> {
        fun bar(): T
    }

    @Suppress("UNUSED")
    interface FooWithTypeDerived : FooWithGenericParameter<String> {
        override fun bar(): String
    }

    @Test
    fun `returns base function with type parameter if passed class is the base class itself`() {
        assertEquals(
            FooWithGenericParameter<*>::bar,
            KFunctionUtil.functionOrDerivedFunction(FooWithGenericParameter<*>::bar, FooWithGenericParameter::class),
        )
    }

    @Test
    fun `returns derived function if passed class is in hierarchy and fills the type parameter from the base class`() {
        assertEquals(
            FooWithTypeDerived::bar,
            KFunctionUtil.functionOrDerivedFunction(FooWithGenericParameter<*>::bar, FooWithTypeDerived::class),
        )
    }
}
