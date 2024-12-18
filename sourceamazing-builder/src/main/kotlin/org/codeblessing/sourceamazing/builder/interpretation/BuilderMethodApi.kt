package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ConceptIdentifierAnnotationData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.schema.ConceptName

abstract class BuilderMethodApi {

    protected abstract fun allBuilderInterpreters(): List<BuilderInterpreter>

    private fun newConceptsAsPair(): List<Pair<Alias, ConceptName>> {
        return allBuilderInterpreters().flatMap { it.getBuilderInterpreterNewConceptsIncludingDuplicates() }
    }

    fun newConcepts(): Map<Alias, ConceptName> {
        return newConceptsAsPair().associate { it }
    }

    fun newConceptAliasesIncludingDuplicates(): List<Alias> {
        return newConceptsAsPair()
            .map { it.first }
    }

    fun newConceptAliases(): Set<Alias> {
        return newConcepts().keys
    }

    fun newConceptByAlias(alias: Alias): ConceptName {
        return requireNotNull(newConcepts()[alias]) {
            "No concept found for alias $alias in ${newConcepts()}."
        }
    }

    fun aliasesToSetRandomConceptIdentifierValueIncludingDuplicates(): List<Alias> {
        return allBuilderInterpreters().flatMap { it.getBuilderInterpreterAliasesToSetRandomConceptIdentifierValueIncludingDuplicates() }
    }

    fun aliasesToSetRandomConceptIdentifierValue(): Set<Alias> {
        return aliasesToSetRandomConceptIdentifierValueIncludingDuplicates().toSet()
    }

    fun aliasesToSetConceptIdentifierValueAliasesIncludingDuplicates(): List<Alias> {
        return allBuilderInterpreters().flatMap { it.getBuilderInterpreterAliasesToSetConceptIdentifierValueAliasesIncludingDuplicates() }
    }

    fun getFacetValueAnnotationContent(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        return allBuilderInterpreters().flatMap { it.getBuilderInterpreterFacetValueAnnotationContent(dataContext) }
    }

    fun getManualAssignedConceptIdentifierAnnotationContent(dataContext: DataContext? = null): List<ConceptIdentifierAnnotationData> {
        return allBuilderInterpreters().flatMap { it.getBuilderInterpreterManualAssignedConceptIdentifierAnnotationContent(dataContext) }
    }
}