package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ClassTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KType

data class JavaReflectionClassTypeMirror(
    private val kType: KType,
    override val classMirror: MirrorProvider<ClassMirrorInterface>,
    override val genericTypeArguments: List<MirrorProvider<TypeMirrorInterface>>,
): JavaReflectionAbstractTypeMirror(kType), ClassTypeMirrorInterface