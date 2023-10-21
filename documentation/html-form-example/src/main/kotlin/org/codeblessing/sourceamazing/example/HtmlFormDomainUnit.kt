package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DefaultDomainUnit
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import java.nio.file.Path
import java.nio.file.Paths

class HtmlFormDomainUnit: DefaultDomainUnit<HtmlFormDomainSchema>(
    schemaDefinitionClass = HtmlFormDomainSchema::class.java
) {

    companion object {
        val outputDirectory: Path = Paths.get("WebContent")
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
