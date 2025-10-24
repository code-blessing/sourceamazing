package org.codeblessing.sourceamazing.schema.typesafeapi

import java.util.*

class ClazzModelId private constructor(val name: Any) {

    companion object {
        fun of(name: Any): ClazzModelId {
            return ClazzModelId(name)
        }

        fun ofRandom(clazz: Clazz, suffix: String = "Generated"): ClazzModelId {
            val uuid = UUID.randomUUID().toString()
            return of("${clazz.simpleName()}-$suffix-$uuid")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false

        if (other is ClazzModelId) {
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
