package org.codeblessing.sourceamazing.api

import org.codeblessing.sourceamazing.api.rules.NameEnforcer

abstract class NamedId protected constructor(name: String): ComparableId(name) {

    init {
        NameEnforcer.isValidNameOrThrow(name)
    }
}
