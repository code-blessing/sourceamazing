package org.codeblessing.sourceamazing.builder.alias

import org.codeblessing.sourceamazing.builder.Alias

fun String.toAlias(): Alias {
    return Alias.of(this)
}
