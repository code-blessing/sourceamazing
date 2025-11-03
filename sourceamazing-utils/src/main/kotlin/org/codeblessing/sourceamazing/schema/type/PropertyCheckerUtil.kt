package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters

object PropertyCheckerUtil {

    fun hasNoValueParameters(propertyToInspect: KProperty<*>): Boolean {
        return propertyToInspect.valueParameters.isNotEmpty()
    }

    fun hasNoExtensionReceiverParameter(propertyToInspect: KProperty<*>): Boolean {
        return propertyToInspect.extensionReceiverParameter != null
    }

    fun hasNoTypeParameter(propertyToInspect: KProperty<*>): Boolean {
        return  propertyToInspect.typeParameters.isNotEmpty()
    }

    fun hasNoFunctionBody(propertyToInspect: KProperty<*>): Boolean {
        return propertyToInspect.isAbstract
    }

    fun hasReturnType(propertyToInspect: KProperty<*>): Boolean {
        val returnType = propertyToInspect.returnType
        return !returnType.isUnitType()
    }
}
