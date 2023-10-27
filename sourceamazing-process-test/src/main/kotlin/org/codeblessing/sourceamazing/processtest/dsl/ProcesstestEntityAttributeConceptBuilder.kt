package org.codeblessing.sourceamazing.processtest.dsl

import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.processtest.EntityAttributeConcept

@DataCollector
interface ProcesstestEntityAttributeConceptBuilder {

    @AddFacet
    fun attributeName(
        @FacetValue attributeName: String,
        @FacetNameValue facetName: FacetName = FacetName.of("AttributeName"),
    ): ProcesstestEntityAttributeConceptBuilder

    @AddFacet
    fun attributeType(
        @FacetValue type: EntityAttributeConcept.AttributeTypeEnum,
        @FacetNameValue facetName: FacetName = FacetName.of("AttributeType"),
    )

}
