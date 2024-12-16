package org.codeblessing.sourceamazing.builder.alias

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS

object BuilderAliasHelper {

    fun defaultAliasHint(conceptAlias: Alias): String {
        val showHint = conceptAlias.name == DEFAULT_CONCEPT_ALIAS
        val hint = "(Hint: The concept alias '${DEFAULT_CONCEPT_ALIAS}' is the default alias and therefore maybe not visible on the annotations)"
        return if(showHint) hint else ""
    }

    fun firstDuplicateAlias(listOfAlias: List<Alias>): Alias? {
        val alreadyUsedAliases: MutableSet<Alias> = mutableSetOf()
        for(alias in listOfAlias) {
            if(alias in alreadyUsedAliases) {
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