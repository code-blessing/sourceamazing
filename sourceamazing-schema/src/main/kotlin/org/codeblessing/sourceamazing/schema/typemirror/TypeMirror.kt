package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider

data class TypeMirror(
    val classMirror: ClassMirrorProvider,
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