package org.codeblessing.sourceamazing.builder.typemirror

import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass

abstract class AbstractSetFixedFacetValueAnnotationMirror(
    annotationClass: KClass<out Annotation>,
    val conceptToModifyAlias: String,
    val facetToModify: MirrorProvider<ClassMirrorInterface>,
    val facetModificationRule: FacetModificationRule,
) : AnnotationMirror(annotationClass)

