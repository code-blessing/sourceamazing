package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
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
        @NewConcept(FormSchema.TextInputFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", FormSchema.FormConcept.FormControl::class, referencedConceptAlias = "formControl")
        @WithNewBuilder(FormControlBuilder::class)
        fun addTextInputFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", FormSchema.FormControl.DisplayName::class) displayName: String,
            @SetFacetValue("formControl", FormSchema.FormControl.ValueRequired::class) valueRequired: Boolean = true,
            @SetFacetValue("formControl", FormSchema.TextInputFormControlConcept.FormatHint::class) formatHint: FormSchema.TextInputFormControlConcept.TextInputFormatHint
        ): FormControlBuilder

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", FormSchema.FormConcept.FormControl::class, referencedConceptAlias = "formControl")
        @WithNewBuilder(SelectDropdownEntryConceptBuilder::class)
        fun addSelectDropdownFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", FormSchema.FormControl.DisplayName::class) displayName: String,
            @SetFacetValue("formControl", FormSchema.FormControl.ValueRequired::class) valueRequired: Boolean = true,
            @SetFacetValue("formControl", FormSchema.SelectDropdownFormControlConcept.DefaultValue::class) defaultValue: String
        ): SelectDropdownEntryConceptBuilder
    }

    interface FormControlBuilderMethods {

        @BuilderMethod
        fun addLabel(
            @SetFacetValue("formControl", FormSchema.FormControl.Label::class) label: String,
        ): FormControlBuilder

        @BuilderMethod
        fun addLabels(
            @SetFacetValue("formControl", FormSchema.FormControl.Label::class) labels: List<String>,
        ): FormControlBuilder

        @BuilderMethod
        fun addLabels(
            @SetFacetValue("formControl", FormSchema.FormControl.Label::class) labels: Array<String>,
        ): FormControlBuilder

        @BuilderMethod
        fun addVariableAmountOfLabels(
            @SetFacetValue("formControl", FormSchema.FormControl.Label::class) vararg labels: String,
        ): FormControlBuilder

    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("formControl")
    interface FormControlBuilder: FormControlBuilderMethods


    @Builder
    @ExpectedAliasFromSuperiorBuilder("formControl")
    interface SelectDropdownEntryConceptBuilder: FormControlBuilderMethods {

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownEntryConcept::class, "dropdownEntry")
        @SetRandomConceptIdentifierValue("dropdownEntry")
        @SetAliasConceptIdentifierReferenceFacetValue("formControl", FormSchema.SelectDropdownFormControlConcept.SelectDropdownEntry::class, referencedConceptAlias = "dropdownEntry")
        fun setValue(
            @SetFacetValue("dropdownEntry", FormSchema.SelectDropdownEntryConcept.Value::class) value: String,
            @SetFacetValue("dropdownEntry", FormSchema.SelectDropdownEntryConcept.DisplayValue::class) displayValue: String = value,
        ): SelectDropdownEntryConceptBuilder
    }

}

