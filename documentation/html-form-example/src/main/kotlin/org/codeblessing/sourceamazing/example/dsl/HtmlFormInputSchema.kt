package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*

@DataCollector
interface HtmlFormInputSchema {

    @ConceptNameValue("HtmlPage")
    @AddConceptAndFacets(HtmlPageBuilder::class)
    @AutoRandomConceptIdentifier
    fun addHtmlPage(@FacetValue("PageTitleName") pageTitle: String, @ConceptBuilder builder: HtmlPageBuilder.() -> Unit)
}