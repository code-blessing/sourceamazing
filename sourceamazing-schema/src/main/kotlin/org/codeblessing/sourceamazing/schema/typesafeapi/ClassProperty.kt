package org.codeblessing.sourceamazing.schema.typesafeapi

class ClassProperty private constructor(val value: String) {

    companion object {
        fun of(clazzProperty: String): ClassProperty {
            return ClassProperty(clazzProperty)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false

        if (other is ClassProperty) {
            return value == other.value
        }
        return false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }

    fun simpleName(): String = this.value

    fun longText(): String = this.value
}
