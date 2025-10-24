package org.codeblessing.sourceamazing.schema.typesafeapi

import kotlin.reflect.KClass

class Clazz private constructor(val clazz: KClass<*>) {

    companion object {
        fun of(clazz: KClass<*>): Clazz {
            return Clazz(clazz)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false

        if (other is Clazz) {
            return this.clazz == other.clazz
        }
        return false
    }

    override fun hashCode(): Int {
        return this.clazz.hashCode()
    }

    override fun toString(): String {
        return simpleName()
    }

    fun simpleName(): String = this.clazz.java.simpleName
}
