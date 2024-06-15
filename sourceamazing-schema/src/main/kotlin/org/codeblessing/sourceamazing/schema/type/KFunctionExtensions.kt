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

@Suppress("UNUSED") fun KFunction<*>.receiverParameter() = parameters.firstOrNull { it.kind == KParameter.Kind.EXTENSION_RECEIVER }
@Suppress("UNUSED") fun KFunction<*>.instanceParameter() = parameters.firstOrNull { it.kind == KParameter.Kind.INSTANCE }
fun KFunction<*>.valueParameters() = parameters.filter { it.kind == KParameter.Kind.VALUE }
fun KFunction<*>.returnTypeOrNull() = if(returnType.isUnitType()) null else returnType

fun KFunction<*>.valueParamsWithValues(args: List<Any?>): List<Pair<KParameter, Any?>> {
    val functionValueParameters = valueParameters()
    require(functionValueParameters.size == args.size) {
        "Method $this parameter number (${functionValueParameters.size} and argument number (${args.size}) not matching."
    }

    return functionValueParameters.mapIndexed { index, parameter -> Pair(parameter, args[index]) }
}
