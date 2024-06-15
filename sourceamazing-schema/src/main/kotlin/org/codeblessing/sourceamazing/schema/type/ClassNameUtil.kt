package org.codeblessing.sourceamazing.schema.type

object ClassNameUtil {

    fun simpleNameFromQualifiedName(qualifiedName: String?): String? {
        if (qualifiedName == null) return null
        return qualifiedName.split(".").last()
    }

    fun packageFromQualifiedName(qualifiedName: String?): String {
        if(qualifiedName == null) return ""
        return qualifiedName.split(".").dropLast(1).joinToString(".")
    }

    fun fullQualifiedName(packageName: String?, className: String?): String? {
        if(className == null) return null
        if(packageName.isNullOrEmpty()) return className
        return "${packageName}.${className}"
    }

}