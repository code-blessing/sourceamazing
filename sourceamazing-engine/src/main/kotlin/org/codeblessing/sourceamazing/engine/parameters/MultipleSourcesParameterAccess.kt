package org.codeblessing.sourceamazing.engine.parameters

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess

class MultipleSourcesParameterAccess(private val parameterSources: List<ParameterSource>): ParameterAccess {

    override fun getParameterMap(): Map<String,String> {
        return parameterSources
            .map { parameterSource -> parameterSource.getParameterMap() }
            .let { mergeMaps(it) }
    }

    override fun getParameter(name: String): String {
        return getParameterOrNull(name) ?: throw IllegalArgumentException("No value for parameter name $name.")
    }

    override fun hasParameter(name: String): Boolean {
        return getParameterOrNull(name) != null
    }

    private fun getParameterOrNull(name: String): String? {
        return getParameterMap()[name]
    }

    private fun mergeMaps(listOfMaps: List<Map<String, String>>): Map<String, String> {
        return listOfMaps
            .flatMap { map -> map.entries }
            .associate(Map.Entry<String, String>::toPair)
    }
}
