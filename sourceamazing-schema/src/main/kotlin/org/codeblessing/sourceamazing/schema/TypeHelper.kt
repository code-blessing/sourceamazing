package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface

object TypeHelper {
    private val kotlinAnyClassMethodNames = setOf(
        ::equals.name,
        ::hashCode.name,
        ::toString.name
    )

    fun isFromKotlinAnyClass(functionMirror: FunctionMirrorInterface): Boolean {
        val functionName = functionMirror.functionName ?: return false
        return kotlinAnyClassMethodNames.contains(functionName)
    }

    fun isNotFromKotlinAnyClass(functionMirror: FunctionMirrorInterface): Boolean {
        return !isFromKotlinAnyClass(functionMirror)
    }
}