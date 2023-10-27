package org.codeblessing.sourceamazing.api.process.datacollection.defaults

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*

@DataCollector
interface DefaultConceptDataCollector {

    // Builder style
    @AddConceptAndFacets(conceptBuilderClazz = DefaultDataCollectorConceptBuilder::class)
    fun newConceptData(
        @ConceptNameValue conceptName: ConceptName,
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier): DefaultDataCollectorConceptBuilder

    // DSL style
    @AddConceptAndFacets(conceptBuilderClazz = DefaultDataCollectorConceptBuilder::class)
    fun newConceptData(
        @ConceptNameValue conceptName: ConceptName,
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
        @ConceptBuilder builder: DefaultDataCollectorConceptBuilder.() -> Unit)

}
