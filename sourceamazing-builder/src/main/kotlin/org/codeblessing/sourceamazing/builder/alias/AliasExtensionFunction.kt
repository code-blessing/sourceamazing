package org.codeblessing.sourceamazing.builder.alias

fun String.toAlias(): Alias {
    return Alias.of(this)
}
