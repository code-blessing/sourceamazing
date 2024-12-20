package org.codeblessing.sourceamazing.schema.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EnumUtilTest {

    private enum class MyEnum { FOO, BAR; }
    private enum class MyOtherEnum { FOO, BUL; }
    private enum class MySubsetEnumEnum { @Suppress("UNUSED") BAR; }
    private enum class MySameEnum { @Suppress("UNUSED") FOO, @Suppress("UNUSED") BAR; }
    private enum class MyEmptyEnum

    @Test
    fun fromStringToEnum() {
        assertEquals(MyEnum.BAR, EnumUtil.fromStringToEnum("BAR", MyEnum::class))
        assertEquals(MyEnum.FOO, EnumUtil.fromStringToEnum("FOO", MyEnum::class))
        assertEquals(MyOtherEnum.BUL, EnumUtil.fromStringToEnum("BUL", MyOtherEnum::class))
        assertEquals(MyOtherEnum.FOO, EnumUtil.fromStringToEnum("FOO", MyOtherEnum::class))
        assertNull(EnumUtil.fromStringToEnum("BAZ", MyEnum::class))
    }

    @Test
    fun isEnumerationType() {
        assertTrue(EnumUtil.isEnumerationType(MyEnum.FOO, MyEnum::class))
        assertTrue(EnumUtil.isEnumerationType(MyEnum.BAR, MyEnum::class))
        assertFalse(EnumUtil.isEnumerationType(MyOtherEnum.FOO, MyEnum::class))
        assertFalse(EnumUtil.isEnumerationType(MyOtherEnum.BUL, MyEnum::class))
        assertFalse(EnumUtil.isEnumerationType("FOO", MyEnum::class))
        assertFalse(EnumUtil.isEnumerationType("x", MyEnum::class))
        assertFalse(EnumUtil.isEnumerationType(42, MyEnum::class))
        assertFalse(EnumUtil.isEnumerationType(false, MyEnum::class))
    }
    @Test
    fun isSameOrSubsetEnumerationClass() {
        assertTrue(EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = MyEnum::class, fullOrSubsetEnumClass = MySameEnum::class))
        assertTrue(EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = MyEnum::class, fullOrSubsetEnumClass = MySubsetEnumEnum::class))
        assertTrue(EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = MyEnum::class, fullOrSubsetEnumClass = MyEmptyEnum::class))
        assertFalse(EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = MyEnum::class, fullOrSubsetEnumClass = MyOtherEnum::class))
        assertFalse(EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = MyEnum::class, fullOrSubsetEnumClass = Any::class))
    }
}