package org.codeblessing.sourceamazing.api.parameter

interface ParameterAccess {
    fun hasParameter(name: String): Boolean
    fun getParameter(name: String): String
    fun getParameterMap(): Map<String,String>
}
