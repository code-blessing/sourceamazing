package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirror

object TypeHelper {

    fun isFromKotlinAnyClass(functionMirror: FunctionMirror): Boolean {
        // TODO make these check more robust by including parameter and return type (or a additional property on FunctionMirror)
        val functionName = functionMirror.functionName ?: return false
        val kotlinAnyClassMethodNames = setOf("equals", "hashCode", "toString")
        return kotlinAnyClassMethodNames.contains(functionName)
    }

    fun isNotFromKotlinAnyClass(functionMirror: FunctionMirror): Boolean {
        return !isFromKotlinAnyClass(functionMirror)
    }
}