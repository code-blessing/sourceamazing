package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ReturnMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import kotlin.reflect.KType

data class JavaReflectionReturnMirror(
    private val ktype: KType
): AbstractMirror(), ReturnMirrorInterface {
    override val type: TypeMirrorInterface = JavaReflectionTypeMirror(ktype)
    override val annotations: List<AnnotationMirror> = JavaReflectionAnnotationHelper.createAnnotationList(ktype.annotations)

}