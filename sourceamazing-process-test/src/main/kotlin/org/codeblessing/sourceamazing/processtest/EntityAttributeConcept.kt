package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@Concept("EntityAttribute")
interface EntityAttributeConcept {
    @Facet("AttributeName")
    fun attributeName(): String

    @Facet("AttributeType")
    fun attributeType(): AttributeTypeEnum

    enum class AttributeTypeEnum {
        TEXT,
        NUMBER,
        BOOLEAN
    }
}
