package org.codeblessing.sourceamazing.engine.process.datacollection

import org.codeblessing.sourceamazing.api.process.datacollection.DomainUnitDataCollectionHelper
import org.codeblessing.sourceamazing.api.process.datacollection.DomainUnitDataCollection
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.ProcessSession

class DomainUnitDataCollectionHelperImpl(private val processSession: ProcessSession, private val schemaAccess: SchemaAccess):
    DomainUnitDataCollectionHelper {
    override fun <I : Any> createDomainUnitDataCollection(
        inputDefinitionClass: Class<I>
    ): DomainUnitDataCollection<I> {
        return DomainUnitDataCollectionImpl(processSession, schemaAccess, inputDefinitionClass)
    }

}
