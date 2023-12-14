package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier

object FormData {

    fun collectFormData(dataCollector: FormBuilder) {

        val employeePreferencesForm = dataCollector
            .createNewForm(
                conceptIdentifier = ConceptIdentifier.of("EmployeeWorkPreferencesForm"),
                formTitle = "Employee Work Preferences")

        employeePreferencesForm
            .addTextInputFormControl(
                conceptIdentifier = ConceptIdentifier.of("EmployeeFirstname"),
                displayName = "Firstname",
                valueRequired = true,
                formatHint = FormSchema.TextInputFormControlConcept.TextInputFormatHint.PLAIN,
            )
            .addTextInputFormControl(
                conceptIdentifier = ConceptIdentifier.of("EmployeeLastname"),
                displayName = "Lastname",
                valueRequired = true,
                formatHint = FormSchema.TextInputFormControlConcept.TextInputFormatHint.PLAIN,
            )
            .addTextInputFormControl(
                conceptIdentifier = ConceptIdentifier.of("EmployeeBirthday"),
                displayName = "Birthday",
                valueRequired = false,
                formatHint = FormSchema.TextInputFormControlConcept.TextInputFormatHint.DATE,
            )

        employeePreferencesForm
            .addSelectDropdownFormControl(
                conceptIdentifier = ConceptIdentifier.of("WorkplacePreference"),
                displayName = "Workplace Preference",
                valueRequired = false,
                defaultValue = "company")
            .setValue("home", "Home Office")
            .setValue("company", "Company Office")
    }

}
