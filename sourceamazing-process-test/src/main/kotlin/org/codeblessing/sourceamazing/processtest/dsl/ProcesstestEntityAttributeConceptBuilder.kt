package org.codeblessing.sourceamazing.processtest.dsl

import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.processtest.EntityAttributeConcept

@DataCollector
interface ProcesstestEntityAttributeConceptBuilder {

    @AddFacets
    fun attributeName(
        @DynamicFacetValue attributeName: String,
        @DynamicFacetNameValue facetName: FacetName = FacetName.of("AttributeName"),
    ): ProcesstestEntityAttributeConceptBuilder

    @AddFacets
    fun attributeType(
        @DynamicFacetValue type: EntityAttributeConcept.AttributeTypeEnum,
        @DynamicFacetNameValue facetName: FacetName = FacetName.of("AttributeType"),
    )

}
