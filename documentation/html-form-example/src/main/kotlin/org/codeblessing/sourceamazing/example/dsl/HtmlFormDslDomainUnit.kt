package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.example.HtmlFormDomainSchema
import org.codeblessing.sourceamazing.example.HtmlPageTemplate
import java.nio.file.Path
import java.nio.file.Paths

class HtmlFormDslDomainUnit: DomainUnit<HtmlFormDomainSchema, HtmlFormInputSchema>(
    schemaDefinitionClass = HtmlFormDomainSchema::class.java,
    inputDefinitionClass = HtmlFormInputSchema::class.java,
) {

    companion object {
        val outputDirectory: Path = Paths.get("WebContent")
    }

    override fun collectInputData(
        parameterAccess: ParameterAccess,
        extensionAccess: DataCollectionExtensionAccess,
        dataCollector: HtmlFormInputSchema
    ) {
        dataCollector.addHtmlPage("Domains") {
            addHtmlSection {
                setSectionName("Domain ")
                addInputField("DNS Domain Name", required = true, maxFieldLength = 255)
            }
            addHtmlSection("Domain Holder Information") {
                addInputField("firstname", required = true, maxFieldLength = 255)
                addInputField("lastname") {
                    setRequired(true)
                    setMaxFieldLength(128)
                }
            }
        }
    }


    override fun collectTargetFiles(
        parameterAccess: ParameterAccess,
        schemaInstance: HtmlFormDomainSchema,
        targetFilesCollector: TargetFilesCollector
    ) {
        val basePath = outputDirectory
        schemaInstance
            .getPageConcepts()
            .forEach { page ->
                val targetFile = basePath.resolve("${page.getHtmlPageTitle()}.html")
                val content = HtmlPageTemplate.createHtmlPageTemplate(page)
                targetFilesCollector.addFile(targetFile, content)
            }
    }
}
