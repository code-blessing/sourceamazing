package org.codeblessing.sourceamazing.processtest.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName

@DataCollector
interface ProcesstestEntityConceptBuilder {

    @AddFacets
    fun name(
        @ValueOfParameterDefinedFacetName entityName: String,
        @ParameterDefinedFacetName facetName: FacetName = FacetName.of("Name"),
    ): ProcesstestEntityConceptBuilder

    @AddFacets
    fun alternativeName(
        @ValueOfParameterDefinedFacetName alternativeName: String?,
        @ParameterDefinedFacetName facetName: FacetName = FacetName.of("AlternativeName"),
    ): ProcesstestEntityConceptBuilder

    @AddConceptAndFacets(conceptBuilderClazz = ProcesstestEntityAttributeConceptBuilder::class)
    fun newEntityAttribute(
        @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier = ConceptIdentifier.random(),
        @ParameterDefinedConceptName conceptName: ConceptName = ConceptName.of("EntityAttribute"),
        ): ProcesstestEntityAttributeConceptBuilder


}
