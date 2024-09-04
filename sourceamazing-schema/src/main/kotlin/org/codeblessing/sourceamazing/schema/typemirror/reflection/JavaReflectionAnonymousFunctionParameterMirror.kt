package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import kotlin.reflect.KType

data class JavaReflectionAnonymousFunctionParameterMirror(
    private val kType: KType
): AbstractMirror(), ParameterMirrorInterface {
    override val name: String? = null
    override val type: TypeMirrorInterface = JavaReflectionMirrorFactory.createTypeMirrorProvider(kType)
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(kType.annotations)
}