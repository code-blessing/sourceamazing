package org.codeblessing.sourceamazing.engine.parameters

import java.util.Properties

object SystemPropertyParameterSource: ParameterSource {

    private val propertyMap: Map<String, String> = PropertiesToMapConverter.convertToMap(
        getPropertiesFromSystemProperties()
    )

    override fun getParameterMap(): Map<String, String> {
        return propertyMap
    }

    private fun getPropertiesFromSystemProperties(): Properties {
        return System.getProperties()
    }

    override fun toString(): String {
        return "${super.toString()}:System-Properties=$propertyMap"
    }

}
