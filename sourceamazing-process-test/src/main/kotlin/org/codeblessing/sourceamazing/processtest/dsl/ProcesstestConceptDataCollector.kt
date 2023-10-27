package org.codeblessing.sourceamazing.processtest.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddConceptAndFacets
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.ConceptIdentifierValue
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.ConceptNameValue
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.DataCollector
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName

@DataCollector
interface ProcesstestConceptDataCollector {

    @AddConceptAndFacets(conceptBuilderClazz = ProcesstestEntityConceptBuilder::class)
    fun newEntity(
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
        @ConceptNameValue conceptName: ConceptName = ConceptName.of("Entity")): ProcesstestEntityConceptBuilder

}
