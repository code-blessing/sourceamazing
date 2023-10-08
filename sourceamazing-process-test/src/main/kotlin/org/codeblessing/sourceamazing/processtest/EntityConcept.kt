package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@Concept("Entity")
interface EntityConcept {

    @Facet("Name")
    fun entityName(): String
    @Facet("AlternativeName", mandatory = false)
    fun entityAlternativeName(): String

    @ChildConcepts(EntityAttributeConcept::class)
    fun getEntityAttributes(): List<EntityAttributeConcept>
}
