package org.codeblessing.sourceamazing.processtest.formschema

object FormData {

    fun collectFormData(dataCollector: FormBuilder) {

        val employeePreferencesForm =
            dataCollector.createNewForm(
                clazzModelId = ItemId.of("EmployeeWorkPreferencesForm"),
                formTitle = "Employee Work Preferences",
            )

        employeePreferencesForm
            .addTextInputFormControl(
                clazzModelId = ItemId.of("EmployeeFirstname"),
                displayName = "Firstname",
                valueRequired = true,
                formatHint = FormSchema.TextInputFormControlClazz.TextInputFormatHint.PLAIN,
            )
            .addLabel("names")
            .addLabel("person-info")
        employeePreferencesForm
            .addTextInputFormControl(
                clazzModelId = ItemId.of("EmployeeLastname"),
                displayName = "Lastname",
                valueRequired = true,
                formatHint = FormSchema.TextInputFormControlClazz.TextInputFormatHint.PLAIN,
            )
            .addLabels(listOf("names", "person-info"))
        employeePreferencesForm
            .addTextInputFormControl(
                clazzModelId = ItemId.of("EmployeeBirthday"),
                displayName = "Birthday",
                valueRequired = false,
                formatHint = FormSchema.TextInputFormControlClazz.TextInputFormatHint.DATE,
            )
            .addLabel("person-info")
            .addLabels(arrayOf("birthday", "Birthday", "BIRTHDAY"))
            .addVariableAmountOfLabels()
            .addVariableAmountOfLabels("b-day")

        employeePreferencesForm
            .addSelectDropdownFormControl(
                clazzModelId = ItemId.of("WorkplacePreference"),
                displayName = "Workplace Preference",
                valueRequired = false,
                defaultValue = "company",
            )
            .setValue("home", "Home Office")
            .setValue("company", "Company Office")
    }
}
