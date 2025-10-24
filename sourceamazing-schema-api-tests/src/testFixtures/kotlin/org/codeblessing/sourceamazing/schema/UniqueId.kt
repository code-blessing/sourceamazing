package org.codeblessing.sourceamazing.schema

/** This object can be used in tests to have an id representation to reference a clazz model. */
data class UniqueId(val name: String) {
    companion object {
        fun of(id: String): UniqueId {
            return UniqueId(id)
        }
    }
}
