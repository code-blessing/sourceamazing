package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
@Schema
interface HtmlFormDomainSchema {

    @ChildConcepts(HtmlPageConcept::class)
    fun getPageConcepts(): List<HtmlPageConcept>
}
