package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.OtherTypeMirrorInterface
import kotlin.reflect.KType


data class JavaReflectionOtherTypeMirror(
    private val kType: KType,
): JavaReflectionAbstractTypeMirror(kType), OtherTypeMirrorInterface