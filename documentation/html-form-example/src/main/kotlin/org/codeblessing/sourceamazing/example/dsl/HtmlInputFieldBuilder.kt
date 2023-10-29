package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddFacets
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.DataCollector
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.FacetValue

@DataCollector
interface HtmlInputFieldBuilder {

    @AddFacets
    fun setRequired(@FacetValue("Required") required: Boolean)

    @AddFacets
    fun setMaxFieldLength(@FacetValue("MaxFieldLength") maxFieldLength: Long)

}