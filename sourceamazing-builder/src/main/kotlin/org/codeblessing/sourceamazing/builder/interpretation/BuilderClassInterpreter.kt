package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.schema.ConceptName
import kotlin.reflect.KClass

class BuilderClassInterpreter(
    val builderClass: KClass<*>,
    private val newConceptNamesWithAliasFromSuperiorBuilder: Map<Alias, ConceptName>,
    ) {

    fun expectedAliasesFromSuperiorBuilderIncludingDuplicates(): List<Alias> {
        return builderClass.annotations.filterIsInstance<ExpectedAliasFromSuperiorBuilder>().map { it.conceptAlias.toAlias() }
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

    fun newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases(): Map<Alias, ConceptName> {
        val expectedAliases = expectedAliasesFromSuperiorBuilder()
        return newConceptNamesFromSuperiorBuilder().filterKeys { key -> key in expectedAliases }
    }
}