package org.codeblessing.sourceamazing.schema.util

import java.lang.reflect.Method

object MethodUtil {
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