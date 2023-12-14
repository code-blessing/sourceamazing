package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConceptId
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryFacet

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
        @Facet(FacetType.TEXT)
        interface FormTitle

        @Facet(FacetType.REFERENCE,
            minimumOccurrences = 1,
            maximumOccurrences = Int.MAX_VALUE,
            referencedConcepts = [TextInputFormControlConcept::class, SelectDropdownFormControlConcept::class])
        interface FormControl

        @QueryConceptId
        fun getFormId(): String

        @QueryFacet(FormTitle::class)
        fun getFormTitle(): String

        @QueryFacet(FormControl::class)
        fun getFormControls(): List<FormSchema.FormControl>
    }

    interface FormControl {

        @Facet(FacetType.TEXT)
        interface DisplayName

        @Facet(FacetType.BOOLEAN)
        interface ValueRequired

        @Facet(FacetType.TEXT,
            minimumOccurrences = 0,
            maximumOccurrences = 3,
        )
        interface Label


        @QueryConceptId
        fun getFormControlName(): String

        @QueryFacet(DisplayName::class)
        fun getFormControlDisplayName(): String

        @QueryFacet(ValueRequired::class)
        fun isValueRequired(): Boolean
    }

    @Concept(facets = [
        FormControl.DisplayName::class,
        FormControl.ValueRequired::class,
        FormControl.Label::class,
        TextInputFormControlConcept.FormatHint::class,
    ])
    interface TextInputFormControlConcept: FormControl {
        @Facet(FacetType.TEXT_ENUMERATION, enumerationClass = TextInputFormatHint::class)
        interface FormatHint

        @QueryFacet(FormatHint::class)
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
        @Facet(FacetType.TEXT, minimumOccurrences = 0)
        interface DefaultValue

        @Facet(FacetType.REFERENCE, minimumOccurrences = 1, maximumOccurrences = 5, referencedConcepts = [SelectDropdownEntryConcept::class])
        interface SelectDropdownEntry

        @QueryFacet(DefaultValue::class)
        fun getDefaultValue(): String?

        @QueryFacet(SelectDropdownEntry::class)
        fun getSelectDropdownEntries(): List<SelectDropdownEntryConcept>

    }

    @Concept(facets = [
        SelectDropdownEntryConcept.Value::class,
        SelectDropdownEntryConcept.DisplayValue::class,
    ])
    interface SelectDropdownEntryConcept {
        @Facet(FacetType.TEXT)
        interface Value
        @Facet(FacetType.TEXT)
        interface DisplayValue

        @QueryFacet(Value::class)
        fun getValue(): String

        @QueryFacet(DisplayValue::class)
        fun getDisplayValue(): String
    }
}

