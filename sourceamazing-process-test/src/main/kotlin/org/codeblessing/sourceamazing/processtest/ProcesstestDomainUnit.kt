package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.processtest.formschema.FormBuilder
import org.codeblessing.sourceamazing.processtest.formschema.FormData
import org.codeblessing.sourceamazing.processtest.formschema.FormSchema
import java.nio.file.Path
import java.nio.file.Paths

class ProcesstestDomainUnit: DomainUnit<FormSchema, FormBuilder>(
    schemaDefinitionClass = FormSchema::class,
    inputDefinitionClass = FormBuilder::class,

) {
    private val defaultXmlPaths = setOf(Paths.get("input-data").resolve("input-data.xml"))

    companion object {
        val outputDirectory: Path = Paths.get("output-data")
    }

    override fun collectTargetFiles(
        parameterAccess: ParameterAccess,
        schemaInstance: FormSchema,
        targetFilesCollector: TargetFilesCollector
    ) {
        val basePath = outputDirectory
        schemaInstance
            .getForms()
            .forEach { form ->
                val targetFile = basePath.resolve("${form.getFormId()}.html")
                targetFilesCollector.addFile(targetFile, ProcesstestTemplate.formContent(form))
            }

        targetFilesCollector.addFile(
            basePath.resolve("forms-description.txt"),
            ProcesstestTemplate.formsSummary(schemaInstance.getForms())
        )

    }

    override fun collectInputData(
        parameterAccess: ParameterAccess,
        extensionAccess: DataCollectionExtensionAccess,
        dataCollector: FormBuilder
    ) {
        extensionAccess.collectWithDataCollectionFromFilesExtension(
            extensionName = ExtensionConstants.xmlSchemaExtensionName,
            inputFiles = defaultXmlPaths,
        )

        FormData.collectFormData(dataCollector)
    }
}
