package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.ComparableId
import org.codeblessing.sourceamazing.api.rules.NameEnforcer
import java.util.*


class ConceptIdentifier private constructor(name: String): org.codeblessing.sourceamazing.api.ComparableId(name = name) {

    init {
        NameEnforcer.isValidIdentifierOrThrow(name)
    }

    companion object {
        fun of(code: String): ConceptIdentifier {
            return ConceptIdentifier(code)
        }

        fun random(): ConceptIdentifier {
            val suffix = UUID.randomUUID().toString().replace("-", "")
            return of("Gen$suffix")
        }
    }
}
