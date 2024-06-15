package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import kotlin.reflect.KParameter

data class JavaReflectionMethodParameterMirror(
    private val parameter: KParameter
): AbstractMirror(), ParameterMirrorInterface {
    override val name: String? = parameter.name
    override val type: TypeMirrorInterface = JavaReflectionMirrorFactory.createTypeMirrorProvider(parameter.type)
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(parameter.annotations)
}