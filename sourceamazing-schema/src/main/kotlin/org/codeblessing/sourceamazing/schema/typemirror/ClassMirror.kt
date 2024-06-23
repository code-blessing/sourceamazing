package org.codeblessing.sourceamazing.schema.typemirror

data class ClassMirror(
    val className: String,
    val isInterface: Boolean,
    val isAnnotation: Boolean,
    val annotations: List<AnnotationMirror>,
    val methods: List<MethodMirror>,
    val propertiesNames: List<String>,

    )