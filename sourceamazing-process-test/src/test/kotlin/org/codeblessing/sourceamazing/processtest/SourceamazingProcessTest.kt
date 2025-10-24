package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.processtest.formschema.FormBuilder
import org.codeblessing.sourceamazing.processtest.formschema.FormData
import org.codeblessing.sourceamazing.processtest.formschema.FormSchema
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SourceamazingProcessTest {

    private val expectedSummaryTemplateOutput =
        """

        Form 'Employee Work Preferences':
        - Form-Control: Display Name: 'Firstname', Labels: [names, person-info]'
        - Form-Control: Display Name: 'Lastname', Labels: [names, person-info]'
        - Form-Control: Display Name: 'Birthday', Labels: [person-info, birthday, Birthday, BIRTHDAY, b-day]'
        - Form-Control: Display Name: 'Workplace Preference', Labels: [] (Default-Value: company) Options: [home -> 'Home Office'], [company -> 'Company Office']
        Text Input Form Control Names: [Firstname, Lastname, Birthday]

        """
            .trimIndent()

    private val expectedHtmlTemplateOutput =
        """
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
        """
            .trimIndent()

    @Test
    fun `run test to import data from builder`() {
        val formSchema =
            SchemaApi.withSchema<FormSchema> { schemaContext ->
                BuilderApi.withBuilder(schemaContext = schemaContext, builderClass = FormBuilder::class) { dataCollector
                    ->
                    FormData.collectFormData(dataCollector)
                }
            }

        Assertions.assertEquals(1, formSchema.forms.size)
        val formSummaryHtml = ProcesstestTemplate.formsSummary(formSchema.forms)

        Assertions.assertEquals(expectedSummaryTemplateOutput, formSummaryHtml)
        val formCitySurveyHtml = ProcesstestTemplate.formContent(formSchema.forms[0])
        Assertions.assertEquals(expectedHtmlTemplateOutput, formCitySurveyHtml)
    }
}
