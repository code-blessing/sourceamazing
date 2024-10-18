package org.codeblessing.sourceamazing.builder.alias

import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.toConceptName
import kotlin.reflect.KFunction

object BuilderAliasHelper {

    fun collectNewConceptsByAlias(method: KFunction<*>): Map<Alias, ConceptName> {
        val newConceptsByAlias: MutableMap<Alias, ConceptName> = mutableMapOf()

        method.annotations.filterIsInstance<NewConcept>().forEach { newConceptAnnotation ->
            val alias = newConceptAnnotation.declareConceptAlias.toAlias()
            val conceptName = newConceptAnnotation.concept.toConceptName()
            newConceptsByAlias[alias] = conceptName
        }

        return newConceptsByAlias
    }
}