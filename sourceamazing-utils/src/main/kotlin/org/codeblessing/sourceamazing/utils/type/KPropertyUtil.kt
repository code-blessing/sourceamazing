package org.codeblessing.sourceamazing.utils.type

import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters

object KPropertyUtil {

    fun hasValueParameters(propertyToInspect: KProperty<*>): Boolean {
        return propertyToInspect.valueParameters.isNotEmpty()
    }

    fun hasExtensionReceiverParameter(propertyToInspect: KProperty<*>): Boolean {
        return propertyToInspect.extensionReceiverParameter != null
    }

    fun hasTypeParameter(propertyToInspect: KProperty<*>): Boolean {
        return  propertyToInspect.typeParameters.isNotEmpty()
    }

    fun hasFunctionBody(propertyToInspect: KProperty<*>): Boolean {
        return !propertyToInspect.isAbstract
    }

    fun hasReturnType(propertyToInspect: KProperty<*>): Boolean {
        val returnType = propertyToInspect.returnType
        return !returnType.isUnitType()
    }
}
