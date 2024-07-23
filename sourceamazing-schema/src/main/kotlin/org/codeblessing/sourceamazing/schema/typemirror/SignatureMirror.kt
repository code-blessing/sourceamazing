package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

/**
 * Represents an item that can be part of a
 * function or property signature like the parameter type
 * or the return type.
 * This may be an ordinary ClassMirror but also
 * a callable (method).
 */
sealed interface SignatureMirror {
    fun toMirrorProvider(): MirrorProvider<out SignatureMirror> {
        return when (this) {
            is FunctionMirror -> this
            is ClassMirror -> this
        }
    }
}