package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AnnotationHelper
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror

object JavaReflectionAnnotationHelper {

    fun createAnnotationList(annotations: List<Annotation>): List<AnnotationMirror> {
        return AnnotationHelper.createAnnotationList(annotations) {
            JavaReflectionMirrorFactory.createClassMirrorProvider(it)
        }
    }
}