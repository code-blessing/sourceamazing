package org.codeblessing.sourceamazing.schema.type

fun Annotation.isAnnotationFromSourceAmazing(): Boolean {
    return this.annotationClass.packageName.startsWith("org.codeblessing")
}
