package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters

object FunctionCheckerUtil {

    fun checkHasNoValueParameters(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.valueParameters.isNotEmpty()) {
            throw WrongFunctionSyntaxException("$functionDescription has parameters. " +
                    "This is not allowed. Function: $functionToInspect")
        }
    }

    fun checkHasNoExtensionReceiverParameter(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.extensionReceiverParameter != null) {
            throw WrongFunctionSyntaxException(
                "$functionDescription has extension receiver parameter. " +
                        "This is not allowed. Function: $functionToInspect"
            )
        }
    }

    fun checkHasNoTypeParameter(functionToInspect: KFunction<*>, functionDescription: String) {
        if(functionToInspect.typeParameters.isNotEmpty()) {
            throw WrongFunctionSyntaxException(
                "$functionDescription has type parameters ${functionToInspect.typeParameters}. " +
                        "This is not allowed. Function: $functionToInspect"
            )
        }
    }

    fun checkHasNoFunctionBody(functionToInspect: KFunction<*>, functionDescription: String) {
        if(!functionToInspect.isAbstract) {
            throw WrongFunctionSyntaxException(
                "$functionDescription must be abstract. Function: $functionToInspect"
            )
        }
    }

}