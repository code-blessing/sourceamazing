package org.codeblessing.sourceamazing.api.process.templating

import kotlin.reflect.KClass

interface DomainUnitProcessTargetFilesHelper {
    fun <S: Any> createDomainUnitProcessTargetFilesData(schemaDefinitionClass: KClass<S>): DomainUnitProcessTargetFilesData<S>

}
