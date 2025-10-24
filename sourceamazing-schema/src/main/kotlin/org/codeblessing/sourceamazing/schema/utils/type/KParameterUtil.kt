package org.codeblessing.sourceamazing.schema.utils.type

import kotlin.reflect.KParameter

object KParameterUtil {

    fun isValueParameter(parameterToInspect: KParameter): Boolean {
        return parameterToInspect.kind == KParameter.Kind.VALUE
    }

    fun isUnitTypeParameter(parameterToInspect: KParameter): Boolean {
        return parameterToInspect.type.isUnitType()
    }

    fun isVarargParameter(parameterToInspect: KParameter): Boolean {
        return parameterToInspect.isVararg
    }

    fun hasParameterName(parameterToInspect: KParameter): Boolean {
        return !parameterToInspect.name.isNullOrBlank()
    }
}
