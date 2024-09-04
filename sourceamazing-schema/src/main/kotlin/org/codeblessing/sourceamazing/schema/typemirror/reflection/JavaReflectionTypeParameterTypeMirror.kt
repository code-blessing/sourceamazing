package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.TypeParameterTypeMirrorInterface
import kotlin.reflect.KType


data class JavaReflectionTypeParameterTypeMirror(
    private val kType: KType,
): JavaReflectionAbstractTypeMirror(kType), TypeParameterTypeMirrorInterface