package org.codeblessing.sourceamazing.api

import org.codeblessing.sourceamazing.api.rules.NameEnforcer

abstract class NamedId protected constructor(name: String): org.codeblessing.sourceamazing.api.ComparableId(name) {

    init {
        NameEnforcer.isValidNameOrThrow(name)
    }
}
