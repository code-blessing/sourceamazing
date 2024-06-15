package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass


class IgnoreNullFacetValueAnnotationMirrorCreator : AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = IgnoreNullFacetValue::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        return IgnoreNullFacetValueAnnotationMirror()
    }
}