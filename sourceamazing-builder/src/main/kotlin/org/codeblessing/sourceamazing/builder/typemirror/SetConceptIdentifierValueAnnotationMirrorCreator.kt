package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass

class SetConceptIdentifierValueAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = SetConceptIdentifierValue::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val setConceptIdentifierValueAnnotation = annotation as SetConceptIdentifierValue
        return SetConceptIdentifierValueAnnotationMirror(
            conceptToModifyAlias = setConceptIdentifierValueAnnotation.conceptToModifyAlias,
        )

    }
}