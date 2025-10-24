package org.codeblessing.sourceamazing.schema


abstract class ComparableStringId protected constructor(val value: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(other == null) return false
        if(other::class != this::class) return false

        if (other is ComparableStringId) {
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
