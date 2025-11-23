package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.processtest.formschema.FormSchema

object ProcesstestTemplate {

    fun formContent(form: FormSchema.FormConcept): String {
        var content = ""

        content += """<html>""" + "\n"
        content += """  <form name="${form.formTitle}">""" + "\n"
        form.formControls.forEach { formControl ->
            if (formControl is FormSchema.TextInputFormControlConcept) {
                content +=
                    """    <label>${formControl.displayName}${if(formControl.valueRequired) "*" else ""}</label>""" +
                        "\n"
                content += """    <input type="text" name="${formControl.displayName}" />""" + "\n"
                content += """    <!-- in form ${form.formTitle} -->""" + "\n"
            } else if (formControl is FormSchema.SelectDropdownFormControlConcept) {
                content +=
                    """    <label>${formControl.displayName}${if(formControl.valueRequired) "*" else ""}</label>""" +
                        "\n"
                content +=
                    """    <select name="${formControl.displayName}" option="${formControl.defaultValue ?: ""}">""" +
                        "\n"
                formControl.selectDropdownEntries.forEach { optionEntry ->
                    content +=
                        """      <option value="${optionEntry.value}">${optionEntry.displayValue}</option>""" +
                            "\n"
                }
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
            content +=
                """
                    
                    Form '${entity.formTitle}':
                    
                    """
                    .trimIndent()

            entity.formControls.forEach { formControl ->
                val formControlSummary =
                    "Form-Control: Display Name: '${formControl.displayName}', Labels: [${formControl.labels.joinToString()}]"

                if (formControl is FormSchema.TextInputFormControlConcept) {
                    content +=
                        """
                        - $formControlSummary'
                        
                        """
                            .trimIndent()
                } else if (formControl is FormSchema.SelectDropdownFormControlConcept) {
                    val options =
                        formControl.selectDropdownEntries.joinToString { optionEntry ->
                            "[${optionEntry.value} -> '${optionEntry.displayValue}']"
                        }
                    content +=
                        """
                        - $formControlSummary (Default-Value: ${formControl.defaultValue}) Options: $options
                        
                        """
                            .trimIndent()
                }
            }

            val listOfTextInputFormControlNames =
                entity.formControls
                    .filterIsInstance<FormSchema.TextInputFormControlConcept>()
                    .joinToString { textInputFormControl -> textInputFormControl.displayName }
            content +=
                """
                    Text Input Form Control Names: [$listOfTextInputFormControlNames]
                    
                """
                    .trimIndent()
        }

        return content
    }
}
