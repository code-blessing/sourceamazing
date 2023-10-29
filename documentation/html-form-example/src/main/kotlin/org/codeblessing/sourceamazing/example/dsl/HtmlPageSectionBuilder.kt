package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@DataCollector
interface HtmlPageSectionBuilder {

    @AddFacets
    fun setSectionName(@FacetValue("SectionName") sectionName: String?)

    @ConceptNameValue("HtmlInputField")
    @AddConceptAndFacets(HtmlInputFieldBuilder::class)
    @AutoRandomConceptIdentifier
    fun addInputField(@FacetValue("FieldName") fieldName: String,
                      @FacetValue("Required") required: Boolean = true,
                      @FacetValue("MaxFieldLength") maxFieldLength: Long = 255)

    @ConceptNameValue("HtmlInputField")
    @AddConceptAndFacets(HtmlInputFieldBuilder::class)
    @AutoRandomConceptIdentifier
    fun addInputField(@FacetValue("FieldName") fieldName: String,
                      @ConceptBuilder builder: HtmlInputFieldBuilder.() -> Unit)

}