package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.NamedId

class ConceptName private constructor(name: String): NamedId(name) {

    companion object {
        fun of(name: String): ConceptName {
            return ConceptName(name)
        }
    }
}
