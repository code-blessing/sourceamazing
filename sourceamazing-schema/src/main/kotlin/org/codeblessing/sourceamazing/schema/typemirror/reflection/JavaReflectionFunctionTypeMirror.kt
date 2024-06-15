package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KType

data class JavaReflectionFunctionTypeMirror(
    private val kType: KType,
    override val functionMirror: MirrorProvider<FunctionMirrorInterface>,
): JavaReflectionAbstractTypeMirror(kType), FunctionTypeMirrorInterface