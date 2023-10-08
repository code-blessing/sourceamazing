package org.codeblessing.sourceamazing.tools

import org.codeblessing.sourceamazing.tools.CaseUtil
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CaseUtilTest {
    @Test
    fun camelToDashCase() {
        assertEquals("my-var-is-foo-bar", CaseUtil.camelToDashCase("myVarIsFooBar"))
        assertEquals("my-var-is-foo-bar", CaseUtil.camelToDashCase("MyVarIsFooBar"))
        assertEquals("a-multi-word-string", CaseUtil.camelToDashCase("AMultiWordString"))
    }

    @Test
    fun camelToSnakeCase() {
        assertEquals("my_var_is_foo_bar", CaseUtil.camelToSnakeCase("myVarIsFooBar"))
        assertEquals("my_var_is_foo_bar", CaseUtil.camelToSnakeCase("MyVarIsFooBar"))
        assertEquals("a_multi_word_string", CaseUtil.camelToSnakeCase("AMultiWordString"))
    }

    @Test
    fun camelToSnakeCaseAllCaps() {
        assertEquals("MY_VAR_IS_FOO_BAR", CaseUtil.camelToSnakeCaseAllCaps("myVarIsFooBar"))
        assertEquals("MY_VAR_IS_FOO_BAR", CaseUtil.camelToSnakeCaseAllCaps("MyVarIsFooBar"))
        assertEquals("A_MULTI_WORD_STRING", CaseUtil.camelToSnakeCaseAllCaps("AMultiWordString"))
    }

    @Test
    fun snakeToLowerCamelCase() {
        assertEquals("myCamelCaseA", CaseUtil.snakeToLowerCamelCase("my_camel_case_a"))
    }

    @Test
    fun snakeToUpperCamelCase() {
        assertEquals("MyCamelCaseA", CaseUtil.snakeToUpperCamelCase("my_camel_case_a"))
    }

    @Test
    fun capitalize() {
        assertEquals("MyWordOfJoy", CaseUtil.capitalize("MyWordOfJoy"))
        assertEquals("MyWordOfJoy", CaseUtil.capitalize("myWordOfJoy"))
        assertEquals("MYWordOfJoy", CaseUtil.capitalize("MYWordOfJoy"))
    }

    @Test
    fun decapitalize() {
        assertEquals("myWordOfJoy", CaseUtil.decapitalize("MyWordOfJoy"))
        assertEquals("myWordOfJoy", CaseUtil.decapitalize("myWordOfJoy"))
        assertEquals("mYWordOfJoy", CaseUtil.decapitalize("MYWordOfJoy"))
    }

}
