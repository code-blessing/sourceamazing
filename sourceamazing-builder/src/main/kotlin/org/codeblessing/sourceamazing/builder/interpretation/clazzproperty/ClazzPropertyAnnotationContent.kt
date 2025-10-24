package org.codeblessing.sourceamazing.builder.interpretation.clazzproperty

sealed interface ClazzPropertyAnnotationContent {
    val base: ClazzPropertyAnnotationBaseData
    val value: Any?
}
