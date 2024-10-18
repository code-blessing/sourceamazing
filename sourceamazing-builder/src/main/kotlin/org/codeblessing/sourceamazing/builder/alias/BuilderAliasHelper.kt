package org.codeblessing.sourceamazing.builder.alias

import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.toConceptName
import kotlin.reflect.KFunction

object BuilderAliasHelper {

    fun collectConceptNameByAlias(method: KFunction<*>): Map<Alias, ConceptName> {
        val newConceptsByAlias: MutableMap<Alias, ConceptName> = mutableMapOf()

        method.annotations.filterIsInstance<NewConcept>().forEach { newConceptAnnotation ->
            newConceptsByAlias[newConceptAnnotation.declareConceptAlias.toAlias()] = newConceptAnnotation.concept.toConceptName()
        }

        return newConceptsByAlias
    }

}