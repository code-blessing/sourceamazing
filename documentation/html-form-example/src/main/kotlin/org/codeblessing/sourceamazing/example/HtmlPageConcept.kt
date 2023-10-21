package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@Concept("HtmlPage")
interface HtmlPageConcept {

    @Facet("PageTitleName")
    fun getHtmlPageTitle(): String

    @ChildConcepts(HtmlPageSectionConcept::class)
    fun getSectionsOfPage(): List<HtmlPageSectionConcept>
}