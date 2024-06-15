package org.codeblessing.sourceamazing.builder.alias

import org.codeblessing.sourceamazing.builder.api.annotations.DEFAULT_CONCEPT_ALIAS
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.toConceptName
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object BuilderAliasHelper {

    fun collectNewConceptAliases(method: KFunction<*>): Map<Alias, ConceptName> {
        return method.annotations
            .filterIsInstance<NewConcept>()
            .associate { Pair(it.declareConceptAlias.toAlias(), it.concept.toConceptName()) }
    }

    fun allAliasesFromExpectedAliasFromSuperiorBuilderAnnotations(builderClass: KClass<*>): List<Alias> {
        return builderClass.annotations.filterIsInstance<ExpectedAliasFromSuperiorBuilder>().map { it.conceptAlias.toAlias() }
    }

    fun filterOnlyExpectedAliasFromSuperiorBuilder(builderClass: KClass<*>, concepts: Map<Alias, ConceptName>): Map<Alias, ConceptName> {
        val expectedAliases = allAliasesFromExpectedAliasFromSuperiorBuilderAnnotations(builderClass).toSet()
        return concepts.filterKeys { key -> key in expectedAliases }
    }

    fun defaultAliasHint(conceptAlias: Alias): String {
        val showHint = conceptAlias.name == DEFAULT_CONCEPT_ALIAS
        val hint = "(Hint: The concept alias '${DEFAULT_CONCEPT_ALIAS}' is the default alias and therefore maybe not visible on the annotations)"
        return if(showHint) hint else ""
    }

}