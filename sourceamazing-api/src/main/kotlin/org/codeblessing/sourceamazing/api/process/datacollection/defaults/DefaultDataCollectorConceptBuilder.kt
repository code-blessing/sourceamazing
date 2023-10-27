package org.codeblessing.sourceamazing.api.process.datacollection.defaults

import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName

@DataCollector
interface DefaultDataCollectorConceptBuilder {

    @AddFacet
    fun addFacetValue(@FacetNameValue facetName: FacetName, @FacetValue facetValue: Any?): DefaultDataCollectorConceptBuilder

    // Builder style
    @AddConcept(conceptBuilderClazz = DefaultDataCollectorConceptBuilder::class)
    fun newConceptData(
        @ConceptNameValue conceptName: ConceptName,
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
    ): DefaultDataCollectorConceptBuilder

    // DSL style
    @AddConcept(conceptBuilderClazz = DefaultDataCollectorConceptBuilder::class)
    fun newConceptData(
        @ConceptNameValue conceptName: ConceptName,
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
        @ConceptBuilder builder: DefaultDataCollectorConceptBuilder.() -> Unit)



}
