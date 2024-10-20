package org.codeblessing.sourceamazing.builder.alias

class Alias private constructor(val name: String) {
    companion object {
        fun of(name: String) = Alias(name)
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alias

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
