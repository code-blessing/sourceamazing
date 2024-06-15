package org.codeblessing.sourceamazing.schema.type

import java.lang.reflect.Method
import java.lang.reflect.Parameter

fun Method.methodParamsWithValues(args: Array<out Any?>): List<Triple<Int, Parameter, Any?>> {
    require(parameterCount == args.size) {
        "Method $this parameter number (${parameterCount} and argument number (${args.size}) not matching."
    }

    return parameters.mapIndexed { index, parameter -> Triple(index, parameter, args[index]) }
}
