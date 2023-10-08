package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.process.datacollection.defaults.DefaultConceptDataCollector
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DefaultDomainUnit
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import java.nio.file.Path
import java.nio.file.Paths

class ProcesstestDomainUnit: DefaultDomainUnit<ProcesstestDomainSchema>(
    schemaDefinitionClass = ProcesstestDomainSchema::class.java
) {

    companion object {
        val outputDirectory: Path = Paths.get("output-data")
    }
    override fun collectTargetFiles(
        parameterAccess: ParameterAccess,
        schemaInstance: ProcesstestDomainSchema,
        targetFilesCollector: TargetFilesCollector
    ) {
        val basePath = outputDirectory
        schemaInstance
            .getEntityConcepts()
            .forEach { entity ->
                val targetFile = basePath.resolve("${entity.entityName()}.example.txt")
                val content = ProcesstestTemplate.createExampleTemplate(targetFile, entity)
                targetFilesCollector.addFile(targetFile, content)
            }

        val targetIndexFile = basePath.resolve("index.example.txt")
        val content = ProcesstestTemplate.createExampleIndexTemplate(targetIndexFile, schemaInstance.getEntityConcepts())
        targetFilesCollector.addFile(targetIndexFile, content)

    }

    override fun collectInputData(
        parameterAccess: ParameterAccess,
        extensionAccess: DataCollectionExtensionAccess,
        dataCollector: DefaultConceptDataCollector
    ) {
        super.collectInputData(parameterAccess, extensionAccess, dataCollector)

        dataCollector
            .newConceptData(ProcesstestEntitySchemaConstants.conceptName, ConceptIdentifier.of("MeinTestkonzept"))
            .addFacetValue(ProcesstestEntitySchemaConstants.entityName,  "MeinTestkonzept-Name")

        dataCollector
            .newConceptData(ProcesstestEntitySchemaConstants.conceptName, ConceptIdentifier.of("MeinZweitesTestkonzept"))
            .addFacetValue(ProcesstestEntitySchemaConstants.entityName,  "MeinZweitesTestkonzept-Name")

    }
}
