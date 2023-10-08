package org.codeblessing.sourceamazing.engine.parameters

import java.util.Properties

object EnvironmentVariablesParameterSource: ParameterSource {

    private val propertyMap: Map<String, String> = PropertiesToMapConverter.convertToMap(
        getPropertiesFromEnvironmentVariables()
    )

    override fun getParameterMap(): Map<String, String> {
        return propertyMap
    }

    private fun getPropertiesFromEnvironmentVariables(): Properties {
        val props = Properties()
        System.getenv().forEach { props[it.key] = it.value }
        return props
    }

    override fun toString(): String {
        return "${super.toString()}:System-Properties=$propertyMap"
    }

}
