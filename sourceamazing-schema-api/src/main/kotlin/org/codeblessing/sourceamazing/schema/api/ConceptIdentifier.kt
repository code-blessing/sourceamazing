package org.codeblessing.sourceamazing.schema.api

import org.codeblessing.sourceamazing.schema.api.rules.NameEnforcer


class ConceptIdentifier private constructor(val name: String) {

    init {
        NameEnforcer.isValidIdentifierOrThrow(name)
    }

    companion object {
        fun of(name: String): ConceptIdentifier {
            return ConceptIdentifier(name)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(other == null) return false
        if(other::class != this::class) return false

        if (other is ConceptIdentifier) {
            return name == other.name
        }
        return false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}:$name"
    }
}
