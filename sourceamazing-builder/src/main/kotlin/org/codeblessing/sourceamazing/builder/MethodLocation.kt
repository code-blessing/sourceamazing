package org.codeblessing.sourceamazing.builder

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

// In the future, there will be the whole call stack for data provider
data class MethodLocation(
    val method: KFunction<*>,
    val methodParameter: KParameter? = null,
) {
    fun locationDescription(): String {
        val paramDescription = methodParameter?.let { ", Parameter: ${it.name}" } ?: ""
        return "Method: $method $paramDescription".trim()
    }
}