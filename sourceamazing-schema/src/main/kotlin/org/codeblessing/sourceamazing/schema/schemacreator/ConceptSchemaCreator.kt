package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.toConceptName

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
