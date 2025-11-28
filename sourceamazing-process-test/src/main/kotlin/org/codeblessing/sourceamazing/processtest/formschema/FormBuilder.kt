package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier

@Builder
@ExpectedAliasFromSuperiorBuilder(concept = FormSchema::class, conceptAlias = "root")
interface FormBuilder {

    @BuilderMethod
    @NewConcept(FormSchema.FormConcept::class, "form")
    @SetAliasConceptIdentifierReferenceFacetValue("root", "forms", referencedConceptAlias = "form")
    fun createNewForm(
        @SetConceptIdentifierValue("form") conceptIdentifier: ConceptIdentifier,
        @SetFacetValue("form", "formTitle") formTitle: String,
    ): FormConceptBuilder

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = FormSchema.FormConcept::class, conceptAlias = "form")
    interface FormConceptBuilder {

        @BuilderMethod
        @NewConcept(FormSchema.TextInputFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", "formControls", referencedConceptAlias = "formControl")
        fun addTextInputFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", "displayName") displayName: String,
            @SetFacetValue("formControl", "valueRequired") valueRequired: Boolean = true,
            @SetFacetValue("formControl", "formatHint")
            formatHint: FormSchema.TextInputFormControlConcept.TextInputFormatHint,
        ): FormControlBuilder

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", "formControls", referencedConceptAlias = "formControl")
        fun addSelectDropdownFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", "displayName") displayName: String,
            @SetFacetValue("formControl", "valueRequired") valueRequired: Boolean = true,
            @SetFacetValue("formControl", "defaultValue") defaultValue: String,
        ): SelectDropdownEntryConceptBuilder
    }

    interface FormControlBuilderMethods<T> {

        @BuilderMethod fun addLabel(@SetFacetValue("formControl", "labels") label: String): T

        @BuilderMethod fun addLabels(@SetFacetValue("formControl", "labels") labels: List<String>): T

        @BuilderMethod fun addLabels(@SetFacetValue("formControl", "labels") labels: Array<String>): T

        @BuilderMethod fun addVariableAmountOfLabels(@SetFacetValue("formControl", "labels") vararg labels: String): T
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(
        concept = FormSchema.TextInputFormControlConcept::class,
        conceptAlias = "formControl",
    )
    interface FormControlBuilder : FormControlBuilderMethods<FormControlBuilder>

    @Builder
    @ExpectedAliasFromSuperiorBuilder(
        concept = FormSchema.SelectDropdownFormControlConcept::class,
        conceptAlias = "formControl",
    )
    interface SelectDropdownEntryConceptBuilder : FormControlBuilderMethods<SelectDropdownEntryConceptBuilder> {

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownEntryConcept::class, "dropdownEntry")
        @SetRandomConceptIdentifierValue("dropdownEntry")
        @SetAliasConceptIdentifierReferenceFacetValue(
            "formControl",
            "selectDropdownEntries",
            referencedConceptAlias = "dropdownEntry",
        )
        fun setValue(
            @SetFacetValue("dropdownEntry", "value") value: String,
            @SetFacetValue("dropdownEntry", "displayValue") displayValue: String = value,
        ): SelectDropdownEntryConceptBuilder
    }
}
