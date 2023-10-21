package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
@Concept("HtmlPageSection")
interface HtmlPageSectionConcept {

    @Facet("SectionName", mandatory = false)
    fun getSectionName(): String?

    @ChildConcepts(HtmlInputFieldConcept::class)
    fun getFieldsInSection(): List<HtmlInputFieldConcept>
}