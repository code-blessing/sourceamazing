package org.codeblessing.sourceamazing.api.process.templating

interface DomainUnitProcessTargetFilesHelper {
    fun <S: Any> createDomainUnitProcessTargetFilesData(schemaDefinitionClass: Class<S>): DomainUnitProcessTargetFilesData<S>

}
