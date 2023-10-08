package org.codeblessing.sourceamazing.api


abstract class ComparableId protected constructor(val name: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(other == null) return false
        if(other::class != this::class) return false

        if (other is org.codeblessing.sourceamazing.api.ComparableId) {
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
