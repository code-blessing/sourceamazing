package org.codeblessing.sourceamazing.engine.process.templating

import org.codeblessing.sourceamazing.api.process.templating.DomainUnitProcessTargetFilesData
import org.codeblessing.sourceamazing.api.process.templating.DomainUnitProcessTargetFilesHelper
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptGraph

class DomainUnitProcessTargetFilesDataHelperImpl(
    private val conceptGraph: ConceptGraph
): DomainUnitProcessTargetFilesHelper {
    override fun <S : Any> createDomainUnitProcessTargetFilesData(schemaDefinitionClass: Class<S>): DomainUnitProcessTargetFilesData<S> {
        return DomainUnitProcessTargetFilesDataImpl(schemaDefinitionClass, conceptGraph)
    }

}
