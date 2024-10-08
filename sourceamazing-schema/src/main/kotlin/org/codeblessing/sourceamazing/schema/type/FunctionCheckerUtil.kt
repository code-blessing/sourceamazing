package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters

object FunctionCheckerUtil {

    fun checkHasNoValueParameters(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.valueParameters.isNotEmpty()) {
            throw WrongFunctionSyntaxException(functionToInspect, "$functionDescription has parameters. This is not allowed.")
        }
    }

    fun checkHasNoExtensionReceiverParameter(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.extensionReceiverParameter != null) {
            throw WrongFunctionSyntaxException(functionToInspect,
                "$functionDescription has extension receiver parameter. This is not allowed."
            )
        }
    }

    fun checkHasNoTypeParameter(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.typeParameters.isNotEmpty()) {
            throw WrongFunctionSyntaxException(functionToInspect,
                "$functionDescription has type parameters ${functionToInspect.typeParameters}. This is not allowed."
            )
        }
    }

    fun checkHasNoFunctionBody(functionToInspect: KFunction<*>, functionDescription: String) {
        if(!functionToInspect.isAbstract) {
            throw WrongFunctionSyntaxException(functionToInspect,
                "$functionDescription must be abstract."
            )
        }
    }

    fun checkHasReturnType(functionToInspect: KFunction<*>, functionDescription: String) {
        val returnType = functionToInspect.returnTypeOrNull()
        if(returnType == null || returnType.isUnitType()) {
            throw WrongFunctionSyntaxException(functionToInspect,
                "$functionDescription must have a return type."
            )
        }
    }
}