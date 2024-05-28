package org.codeblessing.sourceamazing.schema.util

import java.lang.reflect.Method
import java.lang.reflect.Parameter

object MethodUtil {

    fun methodParamsWithValues(method: Method, args: Array<out Any>): List<Triple<Int, Parameter, Any>> {
        require(method.parameterCount == args.size) {
            "Method $method parameter number (${method.parameterCount} and argument number (${args.size}) not matching."
        }

        return method.parameters.mapIndexed { index, parameter -> Triple(index, parameter, args[index]) }
    }

    private enum class ReturnType {
        SINGLE_INSTANCE,
        LIST,
        SET,
    }

    private fun methodReturnType(method: Method): ReturnType {
        return when(method.returnType) {
            List::class.java -> ReturnType.LIST
            Set::class.java -> ReturnType.SET
            else -> ReturnType.SINGLE_INSTANCE
        }
    }

    fun toMethodReturnType(method: Method, resultList: List<Any>): Any? {
        return when(methodReturnType(method)) {
            ReturnType.SINGLE_INSTANCE -> resultList.firstOrNull()
            ReturnType.LIST -> resultList
            ReturnType.SET -> resultList.toSet()
        }
    }
}