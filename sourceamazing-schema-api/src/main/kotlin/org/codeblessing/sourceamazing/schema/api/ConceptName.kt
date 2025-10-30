package org.codeblessing.sourceamazing.schema.api

import kotlin.reflect.KClass

class ConceptName private constructor(val clazz: KClass<*>) {

    companion object {
        fun of(concept: KClass<*>): ConceptName {
            return ConceptName(concept)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(other == null) return false
        if(other::class != this::class) return false

        if (other is ConceptName) {
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
