package org.codeblessing.sourceamazing.api.parameter

interface ParameterAccess {
    fun getParameter(name: String): String
    fun getParameterMap(): Map<String,String>
}
