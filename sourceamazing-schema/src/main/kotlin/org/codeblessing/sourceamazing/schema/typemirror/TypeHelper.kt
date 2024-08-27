package org.codeblessing.sourceamazing.schema.typemirror

object TypeHelper {
    val kotlinAnyClassMethodNames = setOf(
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