package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.ReferenceFacetSchema
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.toConceptName

object SchemaCreator {

    @Throws(SyntaxException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: KClass<*>): SchemaImpl {
        val concepts: MutableMap<ConceptName, ConceptSchema> = mutableMapOf()

        val conceptClassesToProcess: MutableSet<KClass<*>> = mutableSetOf(schemaDefinitionClass)
        while (conceptClassesToProcess.isNotEmpty()) {
            val conceptSchema = ConceptSchemaCreator.createConceptSchema(conceptClassesToProcess.removeFirst())
            concepts.put(conceptSchema.conceptName, conceptSchema)

            conceptSchema.facets
                .filterIsInstance<ReferenceFacetSchema>()
                .flatMap { it.referencingConcepts }
                .filterNot { it in concepts }
                .map { it.clazz }
                .let { conceptClassesToProcess.addAll(it) }
        }

        return SchemaImpl(schemaDefinitionClass.toConceptName(), concepts)
    }

    private fun <T> MutableSet<T>.removeFirst(): T {
        val first = this.first()
        this.remove(first)
        return first
    }
}
