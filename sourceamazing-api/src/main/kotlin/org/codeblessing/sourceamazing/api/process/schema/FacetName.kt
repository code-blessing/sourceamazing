package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.NamedId

class FacetName private constructor(name: String): NamedId(name) {

    companion object {
        fun of(name: String): FacetName {
            return FacetName(name)
        }
    }
}
