package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.SignatureMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

data class JavaReflectionTypeMirror(
    private val kType: KType
): TypeMirrorInterface {
    override val signatureMirror: MirrorProvider<out SignatureMirror> = JavaReflectionMirrorFactory.createClassMirrorProvider(kType.jvmErasure) // TODO correct for java reflection?
    override val nullable: Boolean = kType.isMarkedNullable
}