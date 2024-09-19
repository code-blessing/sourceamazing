package org.codeblessing.sourceamazing.schema.type

object ClassNameUtil {

    fun simpleNameFromQualifiedName(qualifiedName: String?): String? {
        // TODO Fix that
        return qualifiedName?.split(".")?.dropLast(1)?.joinToString(".")
    }

    fun packageFromQualifiedName(qualifiedName: String?): String {
        // see https://youtrack.jetbrains.com/issue/KT-18104
        return qualifiedName?.split(".")?.dropLast(1)?.joinToString(".") ?: ""
    }

    fun fullQualifiedName(packageName: String?, className: String?): String? {
        if(className == null) return null
        if(packageName.isNullOrEmpty()) return className
        return "${packageName}.${className}"
    }

}