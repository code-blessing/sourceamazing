package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.schema.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue

@Schema(concepts = [
    FormSchema.FormConcept::class,
    FormSchema.TextInputFormControlConcept::class,
    FormSchema.SelectDropdownFormControlConcept::class,
    FormSchema.SelectDropdownEntryConcept::class,
])
interface FormSchema {
    @QueryConcepts(conceptClasses = [FormConcept::class])
    fun getForms(): List<FormConcept>


    @Concept(facets = [
        FormConcept.FormTitle::class,
        FormConcept.FormControl::class,
    ])
    interface FormConcept {
        @StringFacet
        interface FormTitle

        @ReferenceFacet(
            minimumOccurrences = 1,
            maximumOccurrences = Int.MAX_VALUE,
            referencedConcepts = [TextInputFormControlConcept::class, SelectDropdownFormControlConcept::class])
        interface FormControl

        @QueryConceptIdentifierValue
        fun getFormId(): String

        @QueryFacetValue(FormTitle::class)
        fun getFormTitle(): String

        @QueryFacetValue(FormControl::class)
        fun getFormControls(): List<FormSchema.FormControl>
    }

    interface FormControl {

        @StringFacet
        interface DisplayName

        @BooleanFacet
        interface ValueRequired

        @StringFacet(
            minimumOccurrences = 0,
            maximumOccurrences = 3,
        )
        interface Label


        @QueryConceptIdentifierValue
        fun getFormControlName(): String

        @QueryFacetValue(DisplayName::class)
        fun getFormControlDisplayName(): String

        @QueryFacetValue(ValueRequired::class)
        fun isValueRequired(): Boolean
    }

    @Concept(facets = [
        FormControl.DisplayName::class,
        FormControl.ValueRequired::class,
        FormControl.Label::class,
        TextInputFormControlConcept.FormatHint::class,
    ])
    interface TextInputFormControlConcept: FormControl {
        @EnumFacet(enumerationClass = TextInputFormatHint::class)
        interface FormatHint

        @QueryFacetValue(FormatHint::class)
        fun getFormatHint(): TextInputFormatHint

        enum class TextInputFormatHint(val hint: String) {
            PLAIN(""),
            MONEY("12.30"),
            DATE("31.12.2023"),
        }

    }

    @Concept(facets = [
        FormControl.DisplayName::class,
        FormControl.ValueRequired::class,
        FormControl.Label::class,
        SelectDropdownFormControlConcept.DefaultValue::class,
        SelectDropdownFormControlConcept.SelectDropdownEntry::class,
    ])

    interface SelectDropdownFormControlConcept: FormControl {
        @StringFacet(minimumOccurrences = 0)
        interface DefaultValue

        @ReferenceFacet(minimumOccurrences = 1, maximumOccurrences = 5, referencedConcepts = [SelectDropdownEntryConcept::class])
        interface SelectDropdownEntry

        @QueryFacetValue(DefaultValue::class)
        fun getDefaultValue(): String?

        @QueryFacetValue(SelectDropdownEntry::class)
        fun getSelectDropdownEntries(): List<SelectDropdownEntryConcept>

    }

    @Concept(facets = [
        SelectDropdownEntryConcept.Value::class,
        SelectDropdownEntryConcept.DisplayValue::class,
    ])
    interface SelectDropdownEntryConcept {
        @StringFacet()
        interface Value
        @StringFacet()
        interface DisplayValue

        @QueryFacetValue(Value::class)
        fun getValue(): String

        @QueryFacetValue(DisplayValue::class)
        fun getDisplayValue(): String
    }
}

