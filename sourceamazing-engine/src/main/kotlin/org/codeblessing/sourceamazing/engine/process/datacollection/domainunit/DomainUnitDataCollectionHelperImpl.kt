package org.codeblessing.sourceamazing.engine.process.datacollection.domainunit

import org.codeblessing.sourceamazing.api.process.datacollection.DomainUnitDataCollection
import org.codeblessing.sourceamazing.api.process.datacollection.DomainUnitDataCollectionHelper
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.ProcessSession
import org.codeblessing.sourceamazing.engine.process.datacollection.builder.DataCollectorBuilderValidator
import kotlin.reflect.KClass

class DomainUnitDataCollectionHelperImpl(private val processSession: ProcessSession, private val schemaAccess: SchemaAccess):
    DomainUnitDataCollectionHelper {
    override fun <I : Any> createDomainUnitDataCollection(
        inputDefinitionClass: KClass<I>
    ): DomainUnitDataCollection<I> {
        DataCollectorBuilderValidator.validateAccessorMethodsOfDataCollector(inputDefinitionClass)
        return DomainUnitDataCollectionImpl(processSession, schemaAccess, inputDefinitionClass)
    }

}
