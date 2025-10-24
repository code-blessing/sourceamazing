package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.Alias

object BuilderRedeclarationHelper {

    fun <T> mapRedeclarations(sourceAliasMap: Map<Alias, T>, aliasRedeclarations: Map<Alias, Alias>): Map<Alias, T> {
        return sourceAliasMap
            .filterNot {
                it.key in aliasRedeclarations.values
            } // remove the values that will be overwritten by redeclarations
            .mapKeys { aliasRedeclarations.getOrDefault(it.key, it.key) } // apply redeclaration
    }
}
