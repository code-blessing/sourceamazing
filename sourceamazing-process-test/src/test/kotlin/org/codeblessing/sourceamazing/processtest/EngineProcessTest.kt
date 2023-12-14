package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.engine.parameters.ParameterSource
import org.codeblessing.sourceamazing.engine.parameters.StaticParameterSource
import org.codeblessing.sourceamazing.engine.process.EngineProcess
import org.codeblessing.sourceamazing.engine.process.ProcessSession
import org.codeblessing.sourceamazing.processtest.formschema.FormBuilder
import org.codeblessing.sourceamazing.processtest.formschema.FormData
import org.codeblessing.sourceamazing.processtest.formschema.FormSchema
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

class EngineProcessTest {

    private val testXmlDefinitionFileContent = """
        <?xml version="1.0" encoding="utf-8" ?>
        <sourceamazing xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema ./schema/sourceamazing-xml-schema.xsd">
            <definitions>
                <formConcept conceptIdentifier="CitySurvey" formTitle="City Survey">
                    <formControl>
                        <textInputFormControlConcept conceptIdentifier="Firstname" displayName="Firstname" valueRequired="true" formatHint="PLAIN">
                            <label>
                                <facetValue value="identity" />
                            </label>
                        </textInputFormControlConcept>
                        <textInputFormControlConcept conceptIdentifier="Lastname" displayName="Lastname" valueRequired="true" formatHint="PLAIN" >
                            <label>
                                <facetValue value="identity" />
                            </label>
                        </textInputFormControlConcept>
                        <selectDropdownFormControlConcept conceptIdentifier="Gender" displayName="Gender" valueRequired="false">
                            <selectDropdownEntry>
                                <selectDropdownEntryConcept value="male" displayValue="Male" />
                                <selectDropdownEntryConcept value="female" displayValue="Female" />
                            </selectDropdownEntry>
                            <label>
                                <facetValue value="identity" />
                            </label>
                        </selectDropdownFormControlConcept>
                        <selectDropdownFormControlConcept conceptIdentifier="Places" displayName="Preferred Place" valueRequired="false" defaultValue="forrest">
                            <selectDropdownEntry>
                                <conceptRef conceptIdentifierReference="DropdownEntryForrest" />
                                <conceptRef conceptIdentifierReference="DropdownEntryCity" />
                            </selectDropdownEntry>
                            <label>
                                <facetValue value="preferences" />
                                <facetValue value="hobbies" />
                            </label>
                        </selectDropdownFormControlConcept>
                        <conceptRef conceptIdentifierReference="Address" />
                    </formControl>
                </formConcept>
                <selectDropdownEntryConcept conceptIdentifier="DropdownEntryForrest" value="forrest" displayValue="Walk in the forrest" />
                <selectDropdownEntryConcept conceptIdentifier="DropdownEntryCity" value="city" displayValue="Hang in the city" />
                <textInputFormControlConcept conceptIdentifier="Address" displayName="Address" valueRequired="true" formatHint="PLAIN" >
                    <label>
                        <facetValue value="identity" />
                    </label>
                </textInputFormControlConcept>
            </definitions>
        </sourceamazing>
    """.trimIndent()

    private val expectedSummaryTemplateOutput = """

        Form 'City Survey':
        - Form-Control: Display Name: 'Firstname'
        - Form-Control: Display Name: 'Lastname'
        - Form-Control: Display Name: 'Gender' (Default-Value: null) Options: [male -> 'Male'], [female -> 'Female']
        - Form-Control: Display Name: 'Preferred Place' (Default-Value: forrest) Options: [forrest -> 'Walk in the forrest'], [city -> 'Hang in the city']
        - Form-Control: Display Name: 'Address'
        Text Input Form Control Names: [Firstname, Lastname, Address]
        
        Form 'Employee Work Preferences':
        - Form-Control: Display Name: 'Firstname'
        - Form-Control: Display Name: 'Lastname'
        - Form-Control: Display Name: 'Birthday'
        - Form-Control: Display Name: 'Workplace Preference' (Default-Value: company) Options: [home -> 'Home Office'], [company -> 'Company Office']
        Text Input Form Control Names: [Firstname, Lastname, Birthday]
        
    """.trimIndent()

    private val expectedHtmlTemplateOutput = """
        <html>
          <form name="CitySurvey">
            <label>Firstname*</label>
            <input type="text" name="Firstname" />
            <!-- in form 'CitySurvey' (City Survey) -->
            <label>Lastname*</label>
            <input type="text" name="Lastname" />
            <!-- in form 'CitySurvey' (City Survey) -->
            <label>Gender</label>
            <select name="Gender" option="">
              <option value="male">Male</option>
              <option value="female">Female</option>
            </select>
            <label>Preferred Place</label>
            <select name="Places" option="forrest">
              <option value="forrest">Walk in the forrest</option>
              <option value="city">Hang in the city</option>
            </select>
            <label>Address*</label>
            <input type="text" name="Address" />
            <!-- in form 'CitySurvey' (City Survey) -->
          </form>
        </html>
    """.trimIndent()


    private val loggingConfigurationClasspath = "/sourceamazing-default-logging.properties"
    private val loggingConfiguration = """
            handlers= java.util.logging.ConsoleHandler
            .level= INFO
            java.util.logging.ConsoleHandler.level = FINE
            java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
            java.util.logging.SimpleFormatter.format=%5${'$'}s%n
    """.trimIndent()

    private val definitionDirectory = FormDomainUnit.xmlDefinitionDirectory
    private val xmlFilename = FormDomainUnit.xmlFilename
    private val defaultOutputDirectory = FormDomainUnit.defaultOutputDirectory
    private val definitionXmlFile = definitionDirectory.resolve(xmlFilename)

    private val classpathResourcesWithContent: Map<String, String> = mapOf(
        loggingConfigurationClasspath to loggingConfiguration
    )
    private val filePathsWithContent: Map<Path, String> = mapOf(
        definitionXmlFile to testXmlDefinitionFileContent
    )

    private val parameterMap: Map<String, String> = emptyMap()



    @Test
    fun `run test domainUnit`() {

        val domainUnits = listOf(FormDomainUnit())
        val fileSystemAccess = StringBasedFileSystemAccess(classpathResourcesWithContent, filePathsWithContent)
        val parameterSources: List<ParameterSource> = listOf(StaticParameterSource(parameterMap))
        val processSession = ProcessSession(
            domainUnits = domainUnits,
            fileSystemAccess = fileSystemAccess,
            parameterSources = parameterSources
        )

        val process = EngineProcess(processSession)

        process.runProcess()

        Assertions.assertTrue(fileSystemAccess.fileExists(defaultOutputDirectory.resolve("forms-description.txt")))
        Assertions.assertTrue(fileSystemAccess.fileExists(defaultOutputDirectory.resolve("CitySurvey.html")))


        Assertions.assertEquals(
            expectedSummaryTemplateOutput,
            fileSystemAccess.fetchFileContent(defaultOutputDirectory.resolve("forms-description.txt"))
        )

        Assertions.assertEquals(
            expectedHtmlTemplateOutput,
            fileSystemAccess.fetchFileContent(defaultOutputDirectory.resolve("CitySurvey.html"))
        )

    }

    class FormDomainUnit: DomainUnit<FormSchema, FormBuilder>(
        schemaDefinitionClass = FormSchema::class,
        inputDefinitionClass = FormBuilder::class,
    ) {
        companion object {
            val xmlDefinitionDirectory: Path = Paths.get("definition/directory")
            const val xmlFilename = "definition-file.xml"
            val defaultOutputDirectory: Path = Paths.get("default/output/directory")

        }

        private val defaultXmlPaths: Set<Path> = setOf(xmlDefinitionDirectory.resolve(xmlFilename))

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

        override fun collectTargetFiles(
            parameterAccess: ParameterAccess,
            schemaInstance: FormSchema,
            targetFilesCollector: TargetFilesCollector
        ) {
            schemaInstance
                .getForms()
                .forEach { form ->
                    val targetFile = defaultOutputDirectory.resolve("${form.getFormId()}.html")
                    targetFilesCollector.addFile(targetFile, ProcesstestTemplate.formContent(form))
                }

            targetFilesCollector.addFile(
                defaultOutputDirectory.resolve("forms-description.txt"),
                ProcesstestTemplate.formsSummary(schemaInstance.getForms())
            )
        }



    }
}
