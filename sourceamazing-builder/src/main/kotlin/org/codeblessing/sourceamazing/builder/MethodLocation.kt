package org.codeblessing.sourceamazing.builder

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmName

// In the future, there will be the whole call stack for data provider
class MethodLocation private constructor(
    private val method: KFunction<*>,
    private val locationElements: List<LocationElement>
) {
    companion object {
        private const val LOCATION_ELEMENT_SEPARATOR = "\n  -> "

        fun create(method: KFunction<*>): MethodLocation {
            return MethodLocation(method, emptyList())
        }
    }

    private data class LocationElement(
        val locationKElement: Any,
        val elementDescription: String,
    )

    fun locationDescription(): String {
        val locationElementsDescription = locationElements.joinToString(separator = LOCATION_ELEMENT_SEPARATOR) { it.elementDescription }
        val locationWithPrefixSeparator = if(locationElementsDescription.isBlank()) "" else "${LOCATION_ELEMENT_SEPARATOR}$locationElementsDescription"
        return "Location:${LOCATION_ELEMENT_SEPARATOR}Builder-Method:[$method]$locationWithPrefixSeparator"
    }

    private fun extendWithLocationElement(locationElement: LocationElement): MethodLocation {
        val newLocationElements = locationElements + locationElement
        return MethodLocation(this.method, newLocationElements)
    }

    fun extendWithMethodParam(methodParameterToAdd: KParameter): MethodLocation {
        val locationElement = LocationElement(methodParameterToAdd, "Parameter:[${methodParameterToAdd.name}]")
        return extendWithLocationElement(locationElement)
    }

    fun extendWithClass(classToAdd: KClass<*>): MethodLocation {
        val locationElement = LocationElement(classToAdd, "Class:[${classToAdd.jvmName}]")
        return extendWithLocationElement(locationElement)
    }

    fun extendWithFunction(functionToAdd: KFunction<*>): MethodLocation {
        val locationElement = LocationElement(functionToAdd, "Function:[${functionToAdd}]")
        return extendWithLocationElement(locationElement)
    }

}