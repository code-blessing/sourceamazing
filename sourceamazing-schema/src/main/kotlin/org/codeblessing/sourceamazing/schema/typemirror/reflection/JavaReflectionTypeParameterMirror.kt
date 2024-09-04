package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.TypeParameterMirrorInterface
import kotlin.reflect.KTypeParameter

data class JavaReflectionTypeParameterMirror(
    private val kTypeParameter: KTypeParameter
): TypeParameterMirrorInterface {
    override val name: String
        get() = kTypeParameter.name
}