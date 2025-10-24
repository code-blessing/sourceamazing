package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.WrongPropertySyntaxException
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters

object PropertyCheckerUtil {

    fun checkHasNoValueParameters(propertyToInspect: KProperty<*>, propertyDescription: String) {
        if(propertyToInspect.valueParameters.isNotEmpty()) {
            throw WrongPropertySyntaxException(propertyToInspect, SchemaErrorCode.FUNCTION_CAN_NOT_HAVE_VALUE_PARAMS, propertyDescription)
        }
    }

    fun checkHasNoExtensionReceiverParameter(propertyToInspect: KProperty<*>, propertyDescription: String) {
        if(propertyToInspect.extensionReceiverParameter != null) {
            throw WrongPropertySyntaxException(propertyToInspect, SchemaErrorCode.FUNCTION_HAS_RECEIVER_PARAM, propertyDescription)
        }
    }

    fun checkHasNoTypeParameter(propertyToInspect: KProperty<*>, propertyDescription: String) {
        if(propertyToInspect.typeParameters.isNotEmpty()) {
            throw WrongPropertySyntaxException(propertyToInspect, SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS, propertyDescription, propertyToInspect.typeParameters)
        }
    }

    fun checkHasNoFunctionBody(propertyToInspect: KProperty<*>, propertyDescription: String) {
        if(!propertyToInspect.isAbstract) {
            throw WrongPropertySyntaxException(propertyToInspect, SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT, propertyDescription)
        }
    }

    fun checkHasReturnType(propertyToInspect: KProperty<*>, propertyDescription: String) {
        val returnType = propertyToInspect.returnType
        if(returnType.isUnitType()) {
            throw WrongPropertySyntaxException(propertyToInspect, SchemaErrorCode.PROPERTY_MUST_HAVE_RETURN_TYPE, propertyDescription)
        }
    }
}
