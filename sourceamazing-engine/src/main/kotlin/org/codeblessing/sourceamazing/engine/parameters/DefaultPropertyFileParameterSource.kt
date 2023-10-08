package org.codeblessing.sourceamazing.engine.parameters

import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import java.util.*

class DefaultPropertyFileParameterSource(private val fileSystemAccess: FileSystemAccess) : ParameterSource {

    private val resourceName = "/sourceamazing.properties"
    private val propertyMap: Map<String, String> = PropertiesToMapConverter.convertToMap(getPropertiesFromFile())

    override fun getParameterMap(): Map<String, String> {
        return propertyMap
    }

    private fun getPropertiesFromFile(): Properties {
        val props = Properties()

        fileSystemAccess.classpathResourceAsInputStream(resourceName).use {
            props.load(it)
        }
        return props
    }

    override fun toString(): String {
        return "${super.toString()}:$resourceName=$propertyMap"
    }

}
