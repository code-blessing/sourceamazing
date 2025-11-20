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
        @SetFacetValue("form", "formTitle") formTitle: String
    ): FormConceptBuilder

    @Builder
    @ExpectedAliasFromSuperiorBuilder("form")
    interface FormConceptBuilder {

        @BuilderMethod
        @NewConcept(FormSchema.TextInputFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", "formControls", referencedConceptAlias = "formControl")
        @WithNewBuilder(FormControlBuilder::class)
        fun addTextInputFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", "displayName") displayName: String,
            @SetFacetValue("formControl", "valueRequired") valueRequired: Boolean = true,
            @SetFacetValue("formControl", "formatHint") formatHint: FormSchema.TextInputFormControlConcept.TextInputFormatHint
        ): FormControlBuilder

        @BuilderMethod
        @NewConcept(FormSchema.SelectDropdownFormControlConcept::class, "formControl")
        @SetAliasConceptIdentifierReferenceFacetValue("form", "formControls", referencedConceptAlias = "formControl")
        @WithNewBuilder(SelectDropdownEntryConceptBuilder::class)
        fun addSelectDropdownFormControl(
            @SetConceptIdentifierValue("formControl") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue("formControl", "displayName") displayName: String,
            @SetFacetValue("formControl", "valueRequired") valueRequired: Boolean = true,
            @SetFacetValue("formControl", "defaultValue") defaultValue: String
        ): SelectDropdownEntryConceptBuilder
    }

    interface FormControlBuilderMethods {

        @BuilderMethod
        fun addLabel(
            @SetFacetValue("formControl", "labels") label: String,
        ): FormControlBuilder

        @BuilderMethod
        fun addLabels(
            @SetFacetValue("formControl", "labels") labels: List<String>,
        ): FormControlBuilder

        @BuilderMethod
        fun addLabels(
            @SetFacetValue("formControl", "labels") labels: Array<String>,
        ): FormControlBuilder

        @BuilderMethod
        fun addVariableAmountOfLabels(
            @SetFacetValue("formControl", "labels") vararg labels: String,
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
        @SetAliasConceptIdentifierReferenceFacetValue("formControl", "selectDropdownEntries", referencedConceptAlias = "dropdownEntry")
        fun setValue(
            @SetFacetValue("dropdownEntry", "value") value: String,
            @SetFacetValue("dropdownEntry", "displayValue") displayValue: String = value,
        ): SelectDropdownEntryConceptBuilder
    }

}

