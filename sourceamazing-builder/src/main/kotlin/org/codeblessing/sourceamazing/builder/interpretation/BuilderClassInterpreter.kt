package org.codeblessing.sourceamazing.builder.interpretation

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.toConceptName

class BuilderClassInterpreter(
    val builderClass: KClass<*>,
    private val newConceptNamesWithAliasFromSuperiorBuilder: Map<Alias, ConceptName>,
) {

    fun expectedAliasesFromSuperiorBuilderIncludingDuplicates(): List<Alias> {
        return builderClass.annotations.filterIsInstance<ExpectedAliasFromSuperiorBuilder>().map {
            it.conceptAlias.toAlias()
        }
    }

    fun expectedAliasesFromSuperiorBuilder(): Set<Alias> {
        return expectedAliasesFromSuperiorBuilderIncludingDuplicates().toSet()
    }

    private fun newConceptNamesFromSuperiorBuilder(): Map<Alias, ConceptName> {
        return newConceptNamesWithAliasFromSuperiorBuilder
    }

    fun newConceptAliasesFromSuperiorBuilder(): Set<Alias> {
        return newConceptNamesFromSuperiorBuilder().keys
    }

    fun expectedAliasesAndConceptNamesFromSuperiorBuilder(): Map<Alias, ConceptName> {
        return builderClass.annotations.filterIsInstance<ExpectedAliasFromSuperiorBuilder>().associate {
            Pair(it.conceptAlias.toAlias(), it.concept.toConceptName())
        }
    }

    fun newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases(): Map<Alias, ConceptName> {
        val expectedAliases = expectedAliasesFromSuperiorBuilder()
        return newConceptNamesFromSuperiorBuilder().filterKeys { key -> key in expectedAliases }
    }
}
