package org.codeblessing.sourceamazing.engine.process.templating.domainunit

import org.codeblessing.sourceamazing.api.process.templating.DomainUnitProcessTargetFilesData
import org.codeblessing.sourceamazing.api.process.templating.DomainUnitProcessTargetFilesHelper
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptGraph
import kotlin.reflect.KClass

class DomainUnitProcessTargetFilesDataHelperImpl(
    private val conceptGraph: ConceptGraph
): DomainUnitProcessTargetFilesHelper {
    override fun <S : Any> createDomainUnitProcessTargetFilesData(schemaDefinitionClass: KClass<S>): DomainUnitProcessTargetFilesData<S> {
        return DomainUnitProcessTargetFilesDataImpl(schemaDefinitionClass, conceptGraph)
    }

}
