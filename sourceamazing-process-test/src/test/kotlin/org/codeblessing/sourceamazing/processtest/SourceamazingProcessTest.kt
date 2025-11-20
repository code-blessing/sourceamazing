package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.processtest.formschema.FormBuilder
import org.codeblessing.sourceamazing.processtest.formschema.FormData
import org.codeblessing.sourceamazing.processtest.formschema.FormSchema
import org.codeblessing.sourceamazing.schema.SchemaProcessor
import org.codeblessing.sourceamazing.schema.api.ConceptData
import org.codeblessing.sourceamazing.schema.api.ConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.api.toConceptName
// import org.codeblessing.sourceamazing.xmlschema.api.XmlSchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

class SourceamazingProcessTest {

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
        
        Form 'Employee Work Preferences':
        - Form-Control: Display Name: 'Firstname', Labels: [names, person-info]'
        - Form-Control: Display Name: 'Lastname', Labels: [names, person-info]'
        - Form-Control: Display Name: 'Birthday', Labels: [person-info, birthday, Birthday, BIRTHDAY, b-day]'
        - Form-Control: Display Name: 'Workplace Preference', Labels: [] (Default-Value: company) Options: [home -> 'Home Office'], [company -> 'Company Office']
        Text Input Form Control Names: [Firstname, Lastname, Birthday]
        
    """.trimIndent()

    private val expectedHtmlTemplateOutput = """
        <html>
          <form name="Employee Work Preferences">
            <label>Firstname*</label>
            <input type="text" name="Firstname" />
            <!-- in form Employee Work Preferences -->
            <label>Lastname*</label>
            <input type="text" name="Lastname" />
            <!-- in form Employee Work Preferences -->
            <label>Birthday</label>
            <input type="text" name="Birthday" />
            <!-- in form Employee Work Preferences -->
            <label>Workplace Preference</label>
            <select name="Workplace Preference" option="company">
              <option value="home">Home Office</option>
              <option value="company">Company Office</option>
            </select>
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


    private val definitionDirectory: Path = Paths.get("definition/directory")
    private val xmlFilename = "definition-file.xml"

    private val definitionXmlFile = definitionDirectory.resolve(xmlFilename)

    private val classpathResourcesWithContent: Map<String, String> = mapOf(
        loggingConfigurationClasspath to loggingConfiguration
    )
    private val filePathsWithContent: Map<Path, String> = mapOf(
        definitionXmlFile to testXmlDefinitionFileContent
    )

    @Test
    fun `run test to import data from xml files and builder`() {
        val fileSystemAccess = StringBasedFileSystemAccess(classpathResourcesWithContent, filePathsWithContent)
        val schemaProcessor = SchemaProcessor(fileSystemAccess)

        val formSchema = schemaProcessor.withSchema(FormSchema::class) { schemaContext ->
            val rootConceptData = schemaContext.dataCollector.newConceptData(FormSchema::class.toConceptName())
            // TODO activate XML schema as soon as it supports root concepts
            // XmlSchemaApi.createXsdSchemaAndReadXmlFile(schemaContext, definitionXmlFile, parameterMap)
            val builderRootAliases = mapOf(
                "root" to rootConceptData.toConceptNameAndIdentifier(),
            )
            BuilderApi.withBuilder(
                schemaContext = schemaContext,
                builderClass = FormBuilder::class,
                builderRootAliases = builderRootAliases,
            ) { dataCollector ->
                FormData.collectFormData(dataCollector)
            }
            rootConceptData.conceptIdentifier
        }

        Assertions.assertEquals(1, formSchema.forms.size)
        val formSummaryHtml = ProcesstestTemplate.formsSummary(formSchema.forms)

        Assertions.assertEquals(expectedSummaryTemplateOutput, formSummaryHtml)
        val formCitySurveyHtml = ProcesstestTemplate.formContent(formSchema.forms[0])
        Assertions.assertEquals(expectedHtmlTemplateOutput, formCitySurveyHtml)
    }

    private fun ConceptData.toConceptNameAndIdentifier(): ConceptNameAndIdentifier {
        return ConceptNameAndIdentifier(
            conceptName = conceptName,
            conceptIdentifier = conceptIdentifier,
        )
    }
}
