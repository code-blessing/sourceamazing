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
@ExpectedAliasFromSuperiorBuilder("root")
interface FormBuilder {

    @BuilderMethod
    @WithNewBuilder(FormConceptBuilder::class)
    @NewConcept(FormSchema.FormConcept::class, "form")
    @SetAliasConceptIdentifierReferenceFacetValue("root", "forms", referencedConceptAlias = "form")
    fun createNewForm(
        @SetConceptIdentifierValue("form") conceptIdentifier: ConceptIdentifier,
        @SetFacetValue("form", "FormTitle") formTitle: String
    ): FormConceptBuilder

    @Builder
    @ExpectedAliasFromSuperiorBuilder("form")
    interface FormConceptBuilder {

        @BuilderMethod
        @NewConcept(FormSchema.TextInputFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", "FormControl", referencedConceptAlias = "formControl")
        @WithNewBuilder(FormControlBuilder::class)
        fun addTextInputFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", "DisplayName") displayName: String,
            @SetFacetValue("formControl", "ValueRequired") valueRequired: Boolean = true,
            @SetFacetValue("formControl", "FormatHint") formatHint: FormSchema.TextInputFormControlConcept.TextInputFormatHint
        ): FormControlBuilder

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", "FormControl", referencedConceptAlias = "formControl")
        @WithNewBuilder(SelectDropdownEntryConceptBuilder::class)
        fun addSelectDropdownFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", "DisplayName") displayName: String,
            @SetFacetValue("formControl", "ValueRequired") valueRequired: Boolean = true,
            @SetFacetValue("formControl", "DefaultValue") defaultValue: String
        ): SelectDropdownEntryConceptBuilder
    }

    interface FormControlBuilderMethods {

        @BuilderMethod
        fun addLabel(
            @SetFacetValue("formControl", "Label") label: String,
        ): FormControlBuilder

        @BuilderMethod
        fun addLabels(
            @SetFacetValue("formControl", "Label") labels: List<String>,
        ): FormControlBuilder

        @BuilderMethod
        fun addLabels(
            @SetFacetValue("formControl", "Label") labels: Array<String>,
        ): FormControlBuilder

        @BuilderMethod
        fun addVariableAmountOfLabels(
            @SetFacetValue("formControl", "Label") vararg labels: String,
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
        @SetAliasConceptIdentifierReferenceFacetValue("formControl", "SelectDropdownEntry", referencedConceptAlias = "dropdownEntry")
        fun setValue(
            @SetFacetValue("dropdownEntry", "Value") value: String,
            @SetFacetValue("dropdownEntry", "DisplayValue") displayValue: String = value,
        ): SelectDropdownEntryConceptBuilder
    }

}

