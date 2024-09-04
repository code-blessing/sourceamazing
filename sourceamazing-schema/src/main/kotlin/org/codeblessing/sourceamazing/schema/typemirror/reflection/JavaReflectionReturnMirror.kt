package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ReturnMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import kotlin.reflect.KType

data class JavaReflectionReturnMirror(
    private val kType: KType
): AbstractMirror(), ReturnMirrorInterface {
    override val type: TypeMirrorInterface = JavaReflectionMirrorFactory.createTypeMirrorProvider(kType)
    // TODO is this annotation list still needed?
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(kType.annotations)

}