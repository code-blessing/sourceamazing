package org.codeblessing.sourceamazing.engine.parameters


class StaticParameterSource(private val staticParameters: Map<String, String>): ParameterSource {

    override fun getParameterMap(): Map<String, String> {
        return staticParameters
    }
}
