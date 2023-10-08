package org.codeblessing.sourceamazing.engine.process.datacollection

import org.codeblessing.sourceamazing.api.process.datacollection.DomainUnitDataCollection
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.ProcessSession
import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.engine.process.datacollection.proxy.DataCollectorInvocationHandler
import org.codeblessing.sourceamazing.engine.extension.ExtensionHolder
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator

class DomainUnitDataCollectionImpl<I: Any>(
    processSession: ProcessSession,
    schemaAccess: SchemaAccess,
    inputDefinitionClass: Class<I>
): DomainUnitDataCollection<I> {

    private val conceptDataCollector: ConceptDataCollector = ConceptDataCollector(schemaAccess, validateConcept = true)
    private val dataCollectorInterface: I = ProxyCreator.createProxy(inputDefinitionClass, DataCollectorInvocationHandler(conceptDataCollector))
    private val extensionAccess: ExtensionHolder = ExtensionHolder(processSession.fileSystemAccess, processSession.loggerFacade, processSession.parameterAccess, schemaAccess, conceptDataCollector)

    override fun getDataCollector(): I {
        return dataCollectorInterface
    }

    override fun getDataCollectionExtensionAccess(): DataCollectionExtensionAccess {
        return extensionAccess
    }

    override fun getCollectedData(): List<ConceptData> {
        return conceptDataCollector.provideConceptData()
    }

}
