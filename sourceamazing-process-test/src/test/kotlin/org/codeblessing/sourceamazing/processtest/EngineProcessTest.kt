package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.datacollection.defaults.DefaultConceptDataCollector
import org.codeblessing.sourceamazing.api.process.DefaultDomainUnit
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.engine.process.EngineProcess
import org.codeblessing.sourceamazing.engine.process.ProcessSession
import org.codeblessing.sourceamazing.engine.parameters.ParameterSource
import org.codeblessing.sourceamazing.engine.parameters.StaticParameterSource
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
                <form conceptIdentifier="FoodCategoriesForm" formTitle="Food Categories Form">
                    <textInputFormControl conceptIdentifier="FoodTextInput" displayName="Food" valueRequired="true" formatHint="PLAIN"/>
                    <selectDropdownFormControl conceptIdentifier="FoodCategorySelect" defaultValue="default" displayName="Category" valueRequired="false">
                        <selectDropdownEntryConcept value="meat" displayValue="Meat" />
                        <selectDropdownEntryConcept value="fish" displayValue="Fish" />
                        <selectDropdownEntryConcept value="vegetable" displayValue="Vegetable" />
                        <selectDropdownEntryConcept value="fruit" displayValue="Fruit" />
                    </selectDropdownFormControl>
                </form>
                <form conceptIdentifier="FoodPopularityForm" formTitle="Popularity of Food">
                    <textInputFormControl displayName="Food" valueRequired="false" formatHint="MONEY"/>
                    <selectDropdownFormControl defaultValue="++" displayName="Popularity" valueRequired="true">
                        <selectDropdownEntryConcept value="+++" displayValue="Loved" />
                        <selectDropdownEntryConcept value="++" displayValue="Eaten" />
                        <selectDropdownEntryConcept value="+" displayValue="Refused" />
                    </selectDropdownFormControl>
                </form>
            </definitions>
        </sourceamazing>
    """.trimIndent()

    private val expectedSummaryTemplateOutput = """
        
        Form 'Employee Work Preferences':
        - Form-Control: Display Name: 'Firstname'
        - Form-Control: Display Name: 'Lastname'
        - Form-Control: Display Name: 'Workplace Preference' (Default-Value: company) Options: [home -> 'Home Office'], [company -> 'Company Office']
        Text Input Form Control Names: [Firstname, Lastname]
        
        Form 'Food Categories Form':
        - Form-Control: Display Name: 'Food'
        - Form-Control: Display Name: 'Category' (Default-Value: default) Options: [meat -> 'Meat'], [fish -> 'Fish'], [vegetable -> 'Vegetable'], [fruit -> 'Fruit']
        Text Input Form Control Names: [Food]
        
        Form 'Popularity of Food':
        - Form-Control: Display Name: 'Food'
        - Form-Control: Display Name: 'Popularity' (Default-Value: ++) Options: [+++ -> 'Loved'], [++ -> 'Eaten'], [+ -> 'Refused']
        Text Input Form Control Names: [Food]
        
    """.trimIndent()

    private val expectedHtmlTemplateOutput = """
        <html>
          <form name="FoodCategoriesForm">
            <label>Food*</label>
            <input type="text" name="FoodTextInput" />
            <!-- in form 'FoodCategoriesForm' (Food Categories Form) -->
            <label>Category</label>
            <select name="FoodCategorySelect" option="default">
              <option value="meat">Meat</option>
              <option value="fish">Fish</option>
              <option value="vegetable">Vegetable</option>
              <option value="fruit">Fruit</option>
            </select>
          </form>
        </html>
    """.trimIndent()


    private val loggingConfigurationClasspath = "/sourceamazing-logging.properties"
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
        Assertions.assertTrue(fileSystemAccess.fileExists(defaultOutputDirectory.resolve("EmployeeWorkPreferencesForm.html")))
        Assertions.assertTrue(fileSystemAccess.fileExists(defaultOutputDirectory.resolve("FoodCategoriesForm.html")))
        Assertions.assertTrue(fileSystemAccess.fileExists(defaultOutputDirectory.resolve("FoodPopularityForm.html")))


        Assertions.assertEquals(
            expectedSummaryTemplateOutput,
            fileSystemAccess.fetchFileContent(defaultOutputDirectory.resolve("forms-description.txt"))
        )

        Assertions.assertEquals(
            expectedHtmlTemplateOutput,
            fileSystemAccess.fetchFileContent(defaultOutputDirectory.resolve("FoodCategoriesForm.html"))
        )

    }

    @Schema
    interface FormSchema {
        @ChildConcepts(FormConcept::class)
        fun getForms(): List<FormConcept>
    }

    @Concept("Form")
    interface FormConcept {
        @ConceptId
        fun getFormId(): String

        @Facet("FormTitle")
        fun getFormTitle(): String
        @ChildConceptsWithCommonBaseInterface(FormControlConcept::class, conceptClasses = [TextInputFormControlConcept::class, SelectDropdownFormControlConcept::class])
        fun getFormControls(): List<FormControlConcept>

        @ChildConcepts(TextInputFormControlConcept::class)
        fun getOnlyTextInputControls(): List<TextInputFormControlConcept>

    }

    interface FormControlConcept {

        @ConceptId
        fun getFormControlName(): String

        @Facet("DisplayName")
        fun getFormControlDisplayName(): String

        @Facet("ValueRequired")
        fun isValueRequired(): Boolean

        @ParentConcept
        fun getParentForm(): FormConcept


    }

    @Concept("TextInputFormControl")
    interface TextInputFormControlConcept: FormControlConcept {

        @Facet("FormatHint")
        fun getFormatHint(): TextInputFormatHint

    }

    enum class TextInputFormatHint(val hint: String) {
        PLAIN(""),
        MONEY("12.30"),
        DATE("31.12.2023"),
    }

    @Concept("SelectDropdownFormControl")
    interface SelectDropdownFormControlConcept: FormControlConcept {
        @Facet("DefaultValue")
        fun getDefaultValue(): String

        @ChildConcepts(SelectDropdownEntryConcept::class)
        fun getSelectDropdownEntries(): List<SelectDropdownEntryConcept>

    }

    @Concept("SelectDropdownEntryConcept")
    interface SelectDropdownEntryConcept {
        @Facet("Value")
        fun getValue(): String

        @Facet("DisplayValue")
        fun getDisplayValue(): String

    }

    class FormDomainUnit: DefaultDomainUnit<FormSchema>(
        schemaDefinitionClass = FormSchema::class.java
    ) {
        companion object {
            val xmlDefinitionDirectory: Path = Paths.get("definition/directory")
            const val xmlFilename = "definition-file.xml"
            val defaultOutputDirectory: Path = Paths.get("default/output/directory")

        }

        override val defaultXmlPaths: Set<Path> = setOf(xmlDefinitionDirectory.resolve(xmlFilename))

        private val formConceptName = ConceptName.of("Form")
        private val textInputFormControlConceptName = ConceptName.of("TextInputFormControl")
        private val selectDropdownFormControlConceptName = ConceptName.of("SelectDropdownFormControl")
        private val selectDropdownEntryConceptName = ConceptName.of("SelectDropdownEntryConcept")
        private val formTitleFacetName = FacetName.of("FormTitle")
        private val formControlDisplayNameFacetName = FacetName.of("DisplayName")
        private val formControlValueRequiredFacetName = FacetName.of("ValueRequired")
        private val textInputFormatHintFacetName = FacetName.of("FormatHint")
        private val selectDropdownDefaultValueFacetName = FacetName.of("DefaultValue")
        private val selectDropdownEntryValueFacetName = FacetName.of("Value")
        private val selectDropdownEntryDisplayNameFacetName = FacetName.of("DisplayValue")

        override fun collectInputData(
            parameterAccess: ParameterAccess,
            extensionAccess: DataCollectionExtensionAccess,
            dataCollector: DefaultConceptDataCollector
        ) {

            val employeePreferencesFormId = ConceptIdentifier.of("EmployeeWorkPreferencesForm")
            dataCollector
                .newConceptData(formConceptName, employeePreferencesFormId)
                .setParent(null)
                .addFacetValue(formTitleFacetName,  "Employee Work Preferences")

            dataCollector
                .newConceptData(textInputFormControlConceptName, ConceptIdentifier.of("EmployeeFirstname"))
                .setParent(employeePreferencesFormId)
                .addFacetValue(formControlDisplayNameFacetName,  "Firstname")
                .addFacetValue(formControlValueRequiredFacetName,  true)
                .addFacetValue(textInputFormatHintFacetName, TextInputFormatHint.PLAIN)

            dataCollector
                .newConceptData(textInputFormControlConceptName, ConceptIdentifier.of("EmployeeLastname"))
                .setParent(employeePreferencesFormId)
                .addFacetValue(formControlDisplayNameFacetName,  "Lastname")
                .addFacetValue(formControlValueRequiredFacetName,  false)
                .addFacetValue(textInputFormatHintFacetName, TextInputFormatHint.MONEY)

            val preferredWorkplaceId = ConceptIdentifier.of("EmployeePreferredWorkplace")
            dataCollector
                .newConceptData(selectDropdownFormControlConceptName, preferredWorkplaceId)
                .setParent(employeePreferencesFormId)
                .addFacetValue(formControlDisplayNameFacetName,  "Workplace Preference")
                .addFacetValue(selectDropdownDefaultValueFacetName,  "company")
                .addFacetValue(formControlValueRequiredFacetName,  true)

            dataCollector
                .newConceptData(selectDropdownEntryConceptName, ConceptIdentifier.of("HomeOffice"))
                .setParent(preferredWorkplaceId)
                .addFacetValue(selectDropdownEntryValueFacetName,  "home")
                .addFacetValue(selectDropdownEntryDisplayNameFacetName,  "Home Office")

            dataCollector
                .newConceptData(selectDropdownEntryConceptName, ConceptIdentifier.of("CompanyOffice"))
                .setParent(preferredWorkplaceId)
                .addFacetValue(selectDropdownEntryValueFacetName,  "company")
                .addFacetValue(selectDropdownEntryDisplayNameFacetName,  "Company Office")

            super.collectInputData(parameterAccess, extensionAccess, dataCollector)
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
                    targetFilesCollector.addFile(targetFile, formContent(form))
                }

            targetFilesCollector.addFile(defaultOutputDirectory.resolve("forms-description.txt"), formsSummary(schemaInstance.getForms()))
        }

        private fun formContent(form: FormConcept): String {
            var content = ""

            content += """<html>""" + "\n"
            content += """  <form name="${form.getFormId()}">""" + "\n"
            form.getFormControls().forEach { formControl ->
                if(formControl is TextInputFormControlConcept) {
                    content += """    <label>${formControl.getFormControlDisplayName()}${if(formControl.isValueRequired()) "*" else ""}</label>""" + "\n"
                    content += """    <input type="text" name="${formControl.getFormControlName()}" />""" + "\n"
                    content += """    <!-- in form '${formControl.getParentForm().getFormId()}' (${formControl.getParentForm().getFormTitle()}) -->""" + "\n"
                } else if (formControl is SelectDropdownFormControlConcept) {
                        content += """    <label>${formControl.getFormControlDisplayName()}${if(formControl.isValueRequired()) "*" else ""}</label>""" + "\n"
                        content += """    <select name="${formControl.getFormControlName()}" option="${formControl.getDefaultValue()}">""" + "\n"
                    formControl.getSelectDropdownEntries()
                        .forEach { optionEntry -> content += """      <option value="${optionEntry.getValue()}">${optionEntry.getDisplayValue()}</option>""" + "\n" }
                    content += """    </select>""" + "\n"
                }
            }
            content += """  </form>""" + "\n"
            content += """</html>"""

            return content
        }

        private fun formsSummary(forms: List<FormConcept>): String {
            var content = ""

            forms.forEach { entity ->
                content += """
                    
                    Form '${entity.getFormTitle()}':
                    
                    """.trimIndent()

                entity.getFormControls().forEach { formControl ->
                    if(formControl is TextInputFormControlConcept) {
                        content += """
                        - Form-Control: Display Name: '${formControl.getFormControlDisplayName()}'
                        
                        """.trimIndent()
                    } else if (formControl is SelectDropdownFormControlConcept) {
                        val options = formControl.getSelectDropdownEntries().joinToString { optionEntry -> "[${optionEntry.getValue()} -> '${optionEntry.getDisplayValue()}']" }
                        content += """
                        - Form-Control: Display Name: '${formControl.getFormControlDisplayName()}' (Default-Value: ${formControl.getDefaultValue()}) Options: $options
                        
                        """.trimIndent()
                    }
                }

                val listOfTextInputFormControlNames = entity.getOnlyTextInputControls().joinToString { textInputFormControl -> textInputFormControl.getFormControlDisplayName() }
                content += """
                    Text Input Form Control Names: [$listOfTextInputFormControlNames]
                    
                """.trimIndent()

            }

            return content
        }

    }
}
