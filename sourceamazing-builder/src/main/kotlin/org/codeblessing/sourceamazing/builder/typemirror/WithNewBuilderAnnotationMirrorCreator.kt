package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass

class WithNewBuilderAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = WithNewBuilder::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val withNewBuilderAnnotation = annotation as WithNewBuilder
        return WithNewBuilderAnnotationMirror(
            builderClass = classMirrorCreatorCallable.classMirrorCreator(withNewBuilderAnnotation.builderClass),
        )

    }
}