package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.processtest.formschema.FormSchema

object ProcesstestTemplate {

    fun formContent(form: FormSchema.FormConcept): String {
        var content = ""

        content += """<html>""" + "\n"
        content += """  <form name="${form.getFormId()}">""" + "\n"
        form.getFormControls().forEach { formControl ->
            if(formControl is FormSchema.TextInputFormControlConcept) {
                content += """    <label>${formControl.getFormControlDisplayName()}${if(formControl.isValueRequired()) "*" else ""}</label>""" + "\n"
                content += """    <input type="text" name="${formControl.getFormControlName()}" />""" + "\n"
                content += """    <!-- in form '${form.getFormId()}' (${form.getFormTitle()}) -->""" + "\n"
            } else if (formControl is FormSchema.SelectDropdownFormControlConcept) {
                content += """    <label>${formControl.getFormControlDisplayName()}${if(formControl.isValueRequired()) "*" else ""}</label>""" + "\n"
                content += """    <select name="${formControl.getFormControlName()}" option="${formControl.getDefaultValue() ?: ""}">""" + "\n"
                formControl.getSelectDropdownEntries()
                    .forEach { optionEntry -> content += """      <option value="${optionEntry.getValue()}">${optionEntry.getDisplayValue()}</option>""" + "\n" }
                content += """    </select>""" + "\n"
            }
        }
        content += """  </form>""" + "\n"
        content += """</html>"""

        return content
    }

    fun formsSummary(forms: List<FormSchema.FormConcept>): String {
        var content = ""

        forms.forEach { entity ->
            content += """
                    
                    Form '${entity.getFormTitle()}':
                    
                    """.trimIndent()

            entity.getFormControls().forEach { formControl ->
                val formControlSummary = "Form-Control: Display Name: '${formControl.getFormControlDisplayName()}', Labels: [${formControl.getLabels().joinToString()}]"

                if(formControl is FormSchema.TextInputFormControlConcept) {
                    content += """
                        - $formControlSummary'
                        
                        """.trimIndent()
                } else if (formControl is FormSchema.SelectDropdownFormControlConcept) {
                    val options = formControl.getSelectDropdownEntries().joinToString { optionEntry -> "[${optionEntry.getValue()} -> '${optionEntry.getDisplayValue()}']" }
                    content += """
                        - $formControlSummary (Default-Value: ${formControl.getDefaultValue()}) Options: $options
                        
                        """.trimIndent()
                }
            }

            val listOfTextInputFormControlNames = entity.getFormControls().filterIsInstance<FormSchema.TextInputFormControlConcept>().joinToString { textInputFormControl -> textInputFormControl.getFormControlDisplayName() }
            content += """
                    Text Input Form Control Names: [$listOfTextInputFormControlNames]
                    
                """.trimIndent()

        }

        return content
    }
}
