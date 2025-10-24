package org.codeblessing.sourceamazing.schema.typesafeapi

fun String.toClazzProperty(): ClassProperty {
    return ClassProperty.of(this)
}
