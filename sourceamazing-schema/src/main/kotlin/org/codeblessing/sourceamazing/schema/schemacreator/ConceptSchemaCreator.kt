package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.toConceptName
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object ConceptSchemaCreator {
    fun createConceptSchema(definitionClass: KClass<*>): ConceptSchema {
        ConceptClassValidator.validateConceptClass(definitionClass)
        val conceptName = definitionClass.toConceptName()

        val facets =
            definitionClass.memberProperties
                .map { FacetSchemaCreator.createFacetSchema(it, conceptName) }
                .onEach { FacetSchemaValidator.validatedFacetSchema(it) }

        return ConceptSchemaImpl(conceptName, facets)
    }
}
