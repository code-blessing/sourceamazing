package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*

@DataCollector
interface HtmlPageBuilder {

    @ConceptNameValue("HtmlPageSection")
    @AddConceptAndFacets(HtmlPageSectionBuilder::class)
    @AutoRandomConceptIdentifier
    fun addHtmlSection(@ConceptBuilder builder: HtmlPageSectionBuilder.() -> Unit)

    @ConceptNameValue("HtmlPageSection")
    @AddConceptAndFacets(HtmlPageSectionBuilder::class)
    @AutoRandomConceptIdentifier
    fun addHtmlSection(@FacetValue("SectionName") sectionName: String, @ConceptBuilder builder: HtmlPageSectionBuilder.() -> Unit)

}