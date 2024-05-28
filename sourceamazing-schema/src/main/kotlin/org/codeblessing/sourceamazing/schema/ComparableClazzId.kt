package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass


abstract class ComparableClazzId protected constructor(val clazz: KClass<*>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(other == null) return false
        if(other::class != this::class) return false

        if (other is ComparableClazzId) {
            return clazz == other.clazz
        }
        return false
    }

    override fun hashCode(): Int {
        return clazz.hashCode()
    }

    override fun toString(): String {
        return clazz.java.simpleName
    }


    fun simpleName(): String = this.clazz.java.simpleName

}
