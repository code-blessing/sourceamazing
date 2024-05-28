package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.ConceptSchema
import org.codeblessing.sourceamazing.schema.SchemaAccess

data class SchemaImpl(
    private val concepts: Map<ConceptName, ConceptSchema>
): SchemaAccess {
    override fun conceptByConceptName(conceptName: ConceptName): ConceptSchema {
        return concepts[conceptName]
            ?: throw IllegalStateException("Concept with name '$conceptName' not found in schema: $concepts")
    }

    override fun hasConceptName(conceptName: ConceptName): Boolean {
        return concepts.containsKey(conceptName)
    }

    override fun allConcepts(): Set<ConceptSchema> {
        return concepts.values.toSet()
    }

    fun numberOfConcepts(): Int {
        return concepts.size
    }
}
