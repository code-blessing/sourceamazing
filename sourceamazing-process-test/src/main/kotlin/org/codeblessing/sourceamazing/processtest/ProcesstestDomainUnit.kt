package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.extensions.ExtensionName
import org.codeblessing.sourceamazing.api.process.datacollection.defaults.DefaultConceptDataCollector
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DefaultDomainUnit
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.processtest.dsl.ProcesstestConceptDataCollector
import java.nio.file.Path
import java.nio.file.Paths

class ProcesstestDomainUnit: DomainUnit<ProcesstestDomainSchema, ProcesstestConceptDataCollector>(
    schemaDefinitionClass = ProcesstestDomainSchema::class.java,
    inputDefinitionClass = ProcesstestConceptDataCollector::class.java,

) {
    private val defaultDataCollectionExtensionName = ExtensionName.of("XmlSchemaInputExtension")
    private val defaultXmlPaths = setOf(Paths.get("input-data").resolve("input-data.xml"))

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
        dataCollector: ProcesstestConceptDataCollector
    ) {
        extensionAccess.collectWithDataCollectionFromFilesExtension(
            extensionName = defaultDataCollectionExtensionName,
            inputFiles = defaultXmlPaths,
        )

        val firstConceptIdentifier = ConceptIdentifier.of("MeinTestkonzept")
        val meinTestkonzept = dataCollector
            .newEntity(conceptIdentifier = firstConceptIdentifier)
            .name(entityName = "MeinTestkonzept-Name")
            .alternativeName(alternativeName = "MeinTestkonzeptli")

        meinTestkonzept
            .newEntityAttribute()
            .attributeName(attributeName = "Anzahl")
            .attributeType(type = EntityAttributeConcept.AttributeTypeEnum.NUMBER)

        meinTestkonzept
            .newEntityAttribute()
            .attributeName(attributeName = "Required")
            .attributeType(type = EntityAttributeConcept.AttributeTypeEnum.BOOLEAN)


        val secondConceptIdentifier = ConceptIdentifier.of("MeinZweitesTestkonzept")
        val meinZweitesTestkonzept = dataCollector
            .newEntity(conceptIdentifier = secondConceptIdentifier)
            .name(entityName = "MeinZweitesTestkonzept-Name")
            .alternativeName(alternativeName = "MeinTestkonzeptli 2")

        meinZweitesTestkonzept
            .newEntityAttribute()
            .attributeName(attributeName = "Kilometer")
            .attributeType(type = EntityAttributeConcept.AttributeTypeEnum.NUMBER)

    }
}
