package org.codeblessing.sourceamazing.builder.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EnumUtilTest {

    private enum class MyEnum {
        FOO,
        BAR,
    }

    private enum class MyOtherEnum {
        FOO,
        BUL,
    }

    private enum class MySubsetEnumEnum {
        @Suppress("UNUSED") BAR
    }

    private enum class MySameEnum {
        @Suppress("UNUSED") FOO,
        @Suppress("UNUSED") BAR,
    }

    private enum class MyEmptyEnum

    @Test
    fun fromStringToEnum() {
        Assertions.assertEquals(MyEnum.BAR, EnumUtil.fromStringToEnum("BAR", MyEnum::class))
        Assertions.assertEquals(MyEnum.FOO, EnumUtil.fromStringToEnum("FOO", MyEnum::class))
        Assertions.assertEquals(MyOtherEnum.BUL, EnumUtil.fromStringToEnum("BUL", MyOtherEnum::class))
        Assertions.assertEquals(MyOtherEnum.FOO, EnumUtil.fromStringToEnum("FOO", MyOtherEnum::class))
        Assertions.assertNull(EnumUtil.fromStringToEnum("BAZ", MyEnum::class))
    }

    @Test
    fun isEnumerationType() {
        Assertions.assertTrue(EnumUtil.isEnumerationType(MyEnum.FOO, MyEnum::class))
        Assertions.assertTrue(EnumUtil.isEnumerationType(MyEnum.BAR, MyEnum::class))
        Assertions.assertFalse(EnumUtil.isEnumerationType(MyOtherEnum.FOO, MyEnum::class))
        Assertions.assertFalse(EnumUtil.isEnumerationType(MyOtherEnum.BUL, MyEnum::class))
        Assertions.assertFalse(EnumUtil.isEnumerationType("FOO", MyEnum::class))
        Assertions.assertFalse(EnumUtil.isEnumerationType("x", MyEnum::class))
        Assertions.assertFalse(EnumUtil.isEnumerationType(42, MyEnum::class))
        Assertions.assertFalse(EnumUtil.isEnumerationType(false, MyEnum::class))
    }

    @Test
    fun isSameOrSubsetEnumerationClass() {
        Assertions.assertTrue(
            EnumUtil.isSameOrSubsetEnumerationClass(
                fullEnumClass = MyEnum::class,
                fullOrSubsetEnumClass = MySameEnum::class,
            )
        )
        Assertions.assertTrue(
            EnumUtil.isSameOrSubsetEnumerationClass(
                fullEnumClass = MyEnum::class,
                fullOrSubsetEnumClass = MySubsetEnumEnum::class,
            )
        )
        Assertions.assertTrue(
            EnumUtil.isSameOrSubsetEnumerationClass(
                fullEnumClass = MyEnum::class,
                fullOrSubsetEnumClass = MyEmptyEnum::class,
            )
        )
        Assertions.assertFalse(
            EnumUtil.isSameOrSubsetEnumerationClass(
                fullEnumClass = MyEnum::class,
                fullOrSubsetEnumClass = MyOtherEnum::class,
            )
        )
        Assertions.assertFalse(
            EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = MyEnum::class, fullOrSubsetEnumClass = Any::class)
        )
    }
}
