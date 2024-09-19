package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

val kotlinAnyClassMethodNames = setOf(
    Any::equals.name,
    Any::hashCode.name,
    Any::toString.name
)

fun KFunction<*>.isFromKotlinAnyClass(): Boolean {
    return kotlinAnyClassMethodNames.contains(this.name)
}

fun KFunction<*>.receiverParameterType() = parameters.firstOrNull { it.kind == KParameter.Kind.EXTENSION_RECEIVER }
fun KFunction<*>.instanceParameterType() = parameters.firstOrNull { it.kind == KParameter.Kind.INSTANCE }
fun KFunction<*>.valueParameters() = parameters.filter { it.kind == KParameter.Kind.VALUE }
fun KFunction<*>.returnTypeOrNull() = if(returnType.isUnitType()) null else returnType
