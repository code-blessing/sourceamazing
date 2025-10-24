package org.codeblessing.sourceamazing.schema.utils.typedeconstruction

class TypeToClassDeconstructionOptions(val allowNullValues: Boolean) {
    companion object {
        val DEFAULT = TypeToClassDeconstructionOptions(allowNullValues = false)
    }
}
