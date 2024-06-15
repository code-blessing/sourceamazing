package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters

object FunctionCheckerUtil {

    fun checkHasNoValueParameters(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.valueParameters.isNotEmpty()) {
            throw WrongFunctionSyntaxException(functionToInspect, SchemaErrorCode.FUNCTION_CAN_NOT_HAVE_VALUE_PARAMS, functionDescription)
        }
    }

    fun checkHasNoExtensionReceiverParameter(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.extensionReceiverParameter != null) {
            throw WrongFunctionSyntaxException(functionToInspect, SchemaErrorCode.FUNCTION_HAS_RECEIVER_PARAM, functionDescription)
        }
    }

    fun checkHasNoTypeParameter(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.typeParameters.isNotEmpty()) {
            throw WrongFunctionSyntaxException(functionToInspect, SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS, functionDescription, functionToInspect.typeParameters)
        }
    }

    fun checkHasNoFunctionBody(functionToInspect: KFunction<*>, functionDescription: String) {
        if(!functionToInspect.isAbstract) {
            throw WrongFunctionSyntaxException(functionToInspect, SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT, functionDescription)
        }
    }

    fun checkHasReturnType(functionToInspect: KFunction<*>, functionDescription: String) {
        val returnType = functionToInspect.returnTypeOrNull()
        if(returnType == null || returnType.isUnitType()) {
            throw WrongFunctionSyntaxException(functionToInspect, SchemaErrorCode.FUNCTION_MUST_HAVE_RETURN_TYPE, functionDescription)
        }
    }
}