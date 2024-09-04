package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass

/**
 * Represents every other annotation that is not provided by source amazing, e.g. [kotlin.Deprecated]
 */
class OtherAnnotationMirror(
    annotationClass: KClass<out Annotation>,
    annotation: Annotation,
) : AnnotationMirror(annotationClass) {
    override fun isAnnotationFromSourceAmazing(): Boolean = false
}