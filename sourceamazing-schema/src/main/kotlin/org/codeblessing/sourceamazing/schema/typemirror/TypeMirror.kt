package org.codeblessing.sourceamazing.schema.typemirror

data class TypeMirror(
    val classMirror: ClassMirror,
    val annotations: List<AnnotationMirror> = emptyList(),
    val nullable: Boolean = false,
) {
    fun withAnnotation(annotation: AnnotationMirror): TypeMirror {
        return this.copy(
            annotations = this.annotations + annotation
        )
    }

    fun nullable(isNullable: Boolean): TypeMirror {
        return copy(
            nullable = isNullable
        )
    }
}