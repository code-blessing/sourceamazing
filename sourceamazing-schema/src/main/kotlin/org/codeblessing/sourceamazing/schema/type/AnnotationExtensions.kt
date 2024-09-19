package org.codeblessing.sourceamazing.schema.type

fun Annotation.isAnnotationFromSourceAmazing(): Boolean {
    return this::class.java.name.startsWith("org.codeblessing")
}
