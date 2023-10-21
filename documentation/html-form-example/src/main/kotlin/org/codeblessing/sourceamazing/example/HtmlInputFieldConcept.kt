package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@Concept("HtmlInputField")
interface HtmlInputFieldConcept {

    @Facet("FieldName")
    fun getFieldName(): String

    @Facet("Required")
    fun isInputRequired(): Boolean

    @Facet("MaxFieldLength")
    fun getMaxFieldLength(): Long

}