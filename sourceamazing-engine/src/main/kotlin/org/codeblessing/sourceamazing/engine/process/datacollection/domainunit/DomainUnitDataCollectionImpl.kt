package org.codeblessing.sourceamazing.engine.process.datacollection.domainunit

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.DomainUnitDataCollection
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.extension.ExtensionHolder
import org.codeblessing.sourceamazing.engine.process.ProcessSession
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.process.datacollection.builder.proxy.DataCollectorInvocationHandler
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import kotlin.reflect.KClass

class DomainUnitDataCollectionImpl<I: Any>(
    processSession: ProcessSession,
    schemaAccess: SchemaAccess,
    inputDefinitionClass: KClass<I>
): DomainUnitDataCollection<I> {

    private val conceptDataCollector: ConceptDataCollector = ConceptDataCollector()
    private val dataCollectorInterface: I = ProxyCreator.createProxy(inputDefinitionClass, DataCollectorInvocationHandler(conceptDataCollector, emptyMap()))
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
