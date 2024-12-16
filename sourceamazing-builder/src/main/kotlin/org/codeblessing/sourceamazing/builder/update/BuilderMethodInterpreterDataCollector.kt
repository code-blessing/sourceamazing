package org.codeblessing.sourceamazing.builder.update

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.schema.ConceptData
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import kotlin.reflect.KParameter

class BuilderMethodInterpreterDataCollector(
    private val conceptDataCollector: ConceptDataCollector,
    val functionArguments: Map<KParameter, Any?>,
    val newConceptIdsFromSuperiorBuilder: Map<Alias, ConceptIdentifier>,
): InterpreterDataCollector {

    private val newConceptIds: MutableMap<Alias, ConceptIdentifier> = mutableMapOf()

    private fun newConceptIds(): Map<Alias, ConceptIdentifier> {
        return newConceptIds
    }

    fun newConceptIdsAndSuperiorConceptIds(): Map<Alias, ConceptIdentifier> {
        return newConceptIdsFromSuperiorBuilder + newConceptIds()
    }

    fun conceptIdByAlias(conceptAlias: Alias): ConceptIdentifier {
        return newConceptIds[conceptAlias] ?: newConceptIdsFromSuperiorBuilder[conceptAlias]
        ?: throw IllegalStateException("Can not find concept id for alias '$conceptAlias'.")
    }

    fun newConceptData(alias: Alias, conceptName: ConceptName, conceptIdentifier: ConceptIdentifier) {
        newConceptIds[alias] = conceptIdentifier
        conceptDataCollector.newConceptData(conceptName, conceptIdentifier)
    }

    fun existingConceptData(conceptId: ConceptIdentifier): ConceptData {
        return conceptDataCollector.existingConceptData(conceptId)
    }

    fun validateAfterUpdate(conceptData: ConceptData) {
        conceptDataCollector.validateAfterUpdate(conceptData)
    }


    fun getDataContext(): DataContext {
        return DataContext(
            functionArguments = functionArguments,
            newConceptIds = newConceptIds,
        )
    }
}