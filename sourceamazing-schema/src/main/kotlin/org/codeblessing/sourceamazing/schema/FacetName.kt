package org.codeblessing.sourceamazing.schema

class FacetName private constructor(val value: String) {

    companion object {
        fun of(facet: String): FacetName {
            return FacetName(facet)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(other == null) return false
        if(other::class != this::class) return false

        if (other is FacetName) {
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
