package org.codeblessing.sourceamazing.processtest.dsl

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*

@DataCollector
interface ProcesstestConceptDataCollector {

    @AddConcept(conceptBuilderClazz = ProcesstestEntityConceptBuilder::class)
    fun newEntity(
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
        @ConceptNameValue conceptName: ConceptName = ConceptName.of("Entity"),
        @ParentConceptIdentifierValue parentConceptIdentifier: ConceptIdentifier? = null): ProcesstestEntityConceptBuilder

}
