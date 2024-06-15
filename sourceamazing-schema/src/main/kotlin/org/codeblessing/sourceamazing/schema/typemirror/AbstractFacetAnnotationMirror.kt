package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.FacetType
import kotlin.reflect.KClass

abstract class AbstractFacetAnnotationMirror(
    annotationClass: KClass<out Annotation>,
    val minimumOccurrences: Int,
    val maximumOccurrences: Int,
    val facetType: FacetType
) : AnnotationMirror(annotationClass)