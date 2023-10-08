package org.codeblessing.sourceamazing.engine.process.templating

import org.codeblessing.sourceamazing.api.process.templating.DomainUnitProcessTargetFilesData
import org.codeblessing.sourceamazing.api.process.templating.TargetFileWithContent
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptGraph
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import org.codeblessing.sourceamazing.engine.process.schema.proxy.SchemaInstanceInvocationHandler

class DomainUnitProcessTargetFilesDataImpl<S: Any>(
    schemaDefinitionClass: Class<S>,
    conceptGraph: ConceptGraph
): DomainUnitProcessTargetFilesData<S> {


    private val targetFilesCollector: ListTargetFilesCollectorImpl = ListTargetFilesCollectorImpl()
    private val schemaInstance = ProxyCreator.createProxy(schemaDefinitionClass, SchemaInstanceInvocationHandler(conceptGraph))

    override fun getSchemaInstance(): S {
        return schemaInstance
    }

    override fun getTargetFilesWithContent(): List<TargetFileWithContent> {
        return targetFilesCollector.getTargetFiles()
    }


    override fun getTargetFilesCollector(): TargetFilesCollector {
        return targetFilesCollector
    }
}
