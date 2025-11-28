package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.alias.Alias

object BuilderAliasHelper {

    fun firstDuplicateAlias(listOfAlias: List<Alias>): Alias? {
        val alreadyUsedAliases: MutableSet<Alias> = mutableSetOf()
        for (alias in listOfAlias) {
            if (alias in alreadyUsedAliases) {
                return alias
            }
            alreadyUsedAliases.add(alias)
        }

        return null // no duplicate
    }

    fun firstMissingAlias(listOfAlias: Collection<Alias>, listOfMaybeMissingAliases: Collection<Alias>): Alias? {
        return listOfAlias.firstOrNull { alias -> alias !in listOfMaybeMissingAliases }
    }
}
