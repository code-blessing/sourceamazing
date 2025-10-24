package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.builder.api.annotations.*

@Builder
@ExpectedClazzModelFromSuperiorBuilder(clazz = FormSchema::class, alias = "root")
interface FormBuilder {

    @BuilderMethod
    @NewClazzModel(FormSchema.FormClazz::class, "form")
    @SetClazzModelOfAlias("root", "forms", referencedAlias = "form")
    fun createNewForm(
        @SetAsClazzModelId("form") clazzModelId: ItemId,
        @SetAsValue("form", "formTitle") formTitle: String,
    ): FormClazzBuilder

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = FormSchema.FormClazz::class, alias = "form")
    interface FormClazzBuilder {

        @BuilderMethod
        @NewClazzModel(FormSchema.TextInputFormControlClazz::class, "formControl")
        @SetClazzModelOfAlias("form", "formControls", referencedAlias = "formControl")
        fun addTextInputFormControl(
            @SetAsClazzModelId("formControl") clazzModelId: ItemId,
            @SetAsValue("formControl", "displayName") displayName: String,
            @SetAsValue("formControl", "valueRequired") valueRequired: Boolean = true,
            @SetAsValue("formControl", "formatHint")
            formatHint: FormSchema.TextInputFormControlClazz.TextInputFormatHint,
        ): FormControlBuilder

        @BuilderMethod
        @NewClazzModel(FormSchema.SelectDropdownFormControlClazz::class, "formControl")
        @SetClazzModelOfAlias("form", "formControls", referencedAlias = "formControl")
        fun addSelectDropdownFormControl(
            @SetAsClazzModelId("formControl") clazzModelId: ItemId,
            @SetAsValue("formControl", "displayName") displayName: String,
            @SetAsValue("formControl", "valueRequired") valueRequired: Boolean = true,
            @SetAsValue("formControl", "defaultValue") defaultValue: String,
        ): SelectDropdownEntryClazzBuilder
    }

    interface FormControlBuilderMethods<T> {

        @BuilderMethod fun addLabel(@SetAsValue("formControl", "labels") label: String): T

        @BuilderMethod fun addLabels(@SetAsValue("formControl", "labels") labels: List<String>): T

        @BuilderMethod fun addLabels(@SetAsValue("formControl", "labels") labels: Array<String>): T

        @BuilderMethod fun addVariableAmountOfLabels(@SetAsValue("formControl", "labels") vararg labels: String): T
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = FormSchema.TextInputFormControlClazz::class, alias = "formControl")
    interface FormControlBuilder : FormControlBuilderMethods<FormControlBuilder>

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(
        clazz = FormSchema.SelectDropdownFormControlClazz::class,
        alias = "formControl",
    )
    interface SelectDropdownEntryClazzBuilder : FormControlBuilderMethods<SelectDropdownEntryClazzBuilder> {

        @BuilderMethod
        @NewClazzModel(FormSchema.SelectDropdownEntryClazz::class, "dropdownEntry")
        @SetClazzModelOfAlias("formControl", "selectDropdownEntries", referencedAlias = "dropdownEntry")
        fun setValue(
            @SetAsValue("dropdownEntry", "value") value: String,
            @SetAsValue("dropdownEntry", "displayValue") displayValue: String = value,
        ): SelectDropdownEntryClazzBuilder
    }
}
