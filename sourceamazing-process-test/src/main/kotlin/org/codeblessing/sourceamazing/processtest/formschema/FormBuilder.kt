package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier

@Builder
interface FormBuilder {

    @BuilderMethod
    @WithNewBuilder(FormConceptBuilder::class)
    @NewConcept(FormSchema.FormConcept::class, "form")
    fun createNewForm(
        @SetConceptIdentifierValue("form") conceptIdentifier: ConceptIdentifier,
        @SetFacetValue("form", FormSchema.FormConcept.FormTitle::class) formTitle: String
    ): FormConceptBuilder

    @Builder
    @ExpectedAliasFromSuperiorBuilder("form")
    interface FormConceptBuilder {

        @BuilderMethod
        @NewConcept(FormSchema.TextInputFormControlConcept::class, "textInput")
        @SetAliasConceptIdentifierReferenceFacetValue("form", FormSchema.FormConcept.FormControl::class, referencedConceptAlias = "textInput")
        fun addTextInputFormControl(
            @SetConceptIdentifierValue("textInput") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("textInput", FormSchema.FormControl.DisplayName::class) displayName: String,
            @SetFacetValue("textInput", FormSchema.FormControl.ValueRequired::class) valueRequired: Boolean = true,
            @SetFacetValue("textInput", FormSchema.TextInputFormControlConcept.FormatHint::class) formatHint: FormSchema.TextInputFormControlConcept.TextInputFormatHint
        ): FormConceptBuilder

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownFormControlConcept::class, "selectDropdown")
        @SetAliasConceptIdentifierReferenceFacetValue("form", FormSchema.FormConcept.FormControl::class, referencedConceptAlias = "selectDropdown")
        @WithNewBuilder(SelectDropdownEntryConceptBuilder::class)
        fun addSelectDropdownFormControl(
            @SetConceptIdentifierValue("selectDropdown") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("selectDropdown", FormSchema.FormControl.DisplayName::class) displayName: String,
            @SetFacetValue("selectDropdown", FormSchema.FormControl.ValueRequired::class) valueRequired: Boolean = true,
            @SetFacetValue("selectDropdown", FormSchema.SelectDropdownFormControlConcept.DefaultValue::class) defaultValue: String
        ): SelectDropdownEntryConceptBuilder
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("selectDropdown")
    interface SelectDropdownEntryConceptBuilder {

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownEntryConcept::class, "dropdownEntry")
        @SetRandomConceptIdentifierValue("dropdownEntry")
        @SetAliasConceptIdentifierReferenceFacetValue("selectDropdown", FormSchema.SelectDropdownFormControlConcept.SelectDropdownEntry::class, referencedConceptAlias = "dropdownEntry")
        fun setValue(
            @SetFacetValue("dropdownEntry", FormSchema.SelectDropdownEntryConcept.Value::class) value: String,
            @SetFacetValue("dropdownEntry", FormSchema.SelectDropdownEntryConcept.DisplayValue::class) displayValue: String = value,
        ): SelectDropdownEntryConceptBuilder
    }

}

