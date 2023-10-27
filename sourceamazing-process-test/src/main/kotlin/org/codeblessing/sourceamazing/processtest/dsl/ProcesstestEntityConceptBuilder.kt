package org.codeblessing.sourceamazing.processtest.dsl

import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName

@DataCollector
interface ProcesstestEntityConceptBuilder {

    @AddFacet
    fun name(
        @FacetValue entityName: String,
        @FacetNameValue facetName: FacetName = FacetName.of("Name"),
    ): ProcesstestEntityConceptBuilder

    @AddFacet
    fun alternativeName(
        @FacetValue alternativeName: String?,
        @FacetNameValue facetName: FacetName = FacetName.of("AlternativeName"),
    ): ProcesstestEntityConceptBuilder

    @AddConcept(conceptBuilderClazz = ProcesstestEntityAttributeConceptBuilder::class)
    fun newEntityAttribute(
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier = ConceptIdentifier.random(),
        @ParentConceptIdentifierValue parentConceptIdentifier: ConceptIdentifier? /* TODO automatic parentId */,
        @ConceptNameValue conceptName: ConceptName = ConceptName.of("EntityAttribute"),
        ): ProcesstestEntityAttributeConceptBuilder


}
