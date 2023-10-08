package org.codeblessing.sourceamazing.api.process

import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.DomainUnitDataCollectionHelper
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.schema.DomainUnitSchemaHelper
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.api.process.templating.DomainUnitProcessTargetFilesHelper
import org.codeblessing.sourceamazing.api.process.templating.TargetFileWithContent
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector

abstract class DomainUnit<S: Any, I: Any>(private val schemaDefinitionClass: Class<S>, private val inputDefinitionClass: Class<I>) {
    fun createSchema(domainUnitSchemaHelper: DomainUnitSchemaHelper): SchemaAccess {
        return domainUnitSchemaHelper.createDomainUnitSchema(schemaDefinitionClass = schemaDefinitionClass)
    }

    fun processDomainUnitInputData(parameterAccess: ParameterAccess, domainUnitDataCollectionHelper: DomainUnitDataCollectionHelper): List<ConceptData> {
        val domainUnitProcessInputData = domainUnitDataCollectionHelper.createDomainUnitDataCollection(inputDefinitionClass = inputDefinitionClass)

        collectInputData(
            parameterAccess = parameterAccess,
            extensionAccess = domainUnitProcessInputData.getDataCollectionExtensionAccess(),
            dataCollector = domainUnitProcessInputData.getDataCollector()
        )
        return domainUnitProcessInputData.getCollectedData()
    }

    fun processDomainUnitTargetFiles(parameterAccess: ParameterAccess, domainUnitProcessTargetFilesHelper: DomainUnitProcessTargetFilesHelper): List<TargetFileWithContent> {
        val domainUnitProcessTargetFilesData = domainUnitProcessTargetFilesHelper.createDomainUnitProcessTargetFilesData(schemaDefinitionClass = schemaDefinitionClass)
        collectTargetFiles(
            parameterAccess = parameterAccess,
            schemaInstance = domainUnitProcessTargetFilesData.getSchemaInstance(),
            targetFilesCollector = domainUnitProcessTargetFilesData.getTargetFilesCollector()
        )
        return domainUnitProcessTargetFilesData.getTargetFilesWithContent()
    }

    abstract fun collectInputData(parameterAccess: ParameterAccess, extensionAccess: DataCollectionExtensionAccess, dataCollector: I)

    abstract fun collectTargetFiles(parameterAccess: ParameterAccess, schemaInstance: S, targetFilesCollector: TargetFilesCollector)
}
