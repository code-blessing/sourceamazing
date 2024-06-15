package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import kotlin.reflect.KClass

class SchemaAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = Schema::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val schemaAnnotation = annotation as Schema
        return SchemaAnnotationMirror(schemaAnnotation.concepts.map(classMirrorCreatorCallable::classMirrorCreator))
    }
}