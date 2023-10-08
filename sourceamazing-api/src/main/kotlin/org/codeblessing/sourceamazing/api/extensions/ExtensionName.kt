package org.codeblessing.sourceamazing.api.extensions

import org.codeblessing.sourceamazing.api.NamedId


class ExtensionName private constructor(name: String): NamedId(name) {

    companion object {
        fun of(name: String): ExtensionName {
            return ExtensionName(name)
        }
    }
}
