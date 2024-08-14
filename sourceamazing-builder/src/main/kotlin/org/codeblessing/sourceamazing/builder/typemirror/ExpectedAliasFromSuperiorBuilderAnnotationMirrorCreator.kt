package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorCreatorCallable
import kotlin.reflect.KClass

class ExpectedAliasFromSuperiorBuilderAnnotationMirrorCreator: AnnotationMirrorCreator {
    override fun annotationClass(): KClass<out Annotation> = ExpectedAliasFromSuperiorBuilder::class

    override fun createAnnotationMirror(
        annotation: Annotation,
        classMirrorCreatorCallable: ClassMirrorCreatorCallable
    ): AnnotationMirror {
        val expectedAliasFromSuperiorBuilderAnnotation = annotation as ExpectedAliasFromSuperiorBuilder
        return ExpectedAliasFromSuperiorBuilderAnnotationMirror(
            conceptAlias = expectedAliasFromSuperiorBuilderAnnotation.conceptAlias,
        )

    }
}