package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptSchema

object ConceptSchemaCreator {
    fun createConceptSchema(definitionClass: KClass<*>): ConceptSchema {
        ConceptClassValidator.validateConceptClass(definitionClass)
        val conceptName = ConceptName.of(definitionClass)

        val facets =
            definitionClass.memberProperties
                .map { FacetSchemaCreator.createFacetSchema(it, conceptName) }
                .onEach { FacetSchemaValidator.validatedFacetSchema(it, conceptName) }

        return ConceptSchemaImpl(conceptName, facets)
    }
}
