package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.schema.api.annotations.*

interface FormSchema {
    @Suppress("UNUSED")
    @Facet val forms: List<FormConcept>

    interface FormConcept {
        @Suppress("UNUSED")
        @Facet val formTitle: String

        @Suppress("UNUSED")
        @Facet
        @References([TextInputFormControlConcept::class, SelectDropdownFormControlConcept::class])
        val formControls: List<FormSchema.FormControl>
    }

    interface FormControl {
        @Suppress("UNUSED")
        @Facet
        val displayName: String

        @Suppress("UNUSED")
        @Facet
        val valueRequired: Boolean

        @Suppress("UNUSED")
        @Facet
        val labels: List<String>
    }

    interface TextInputFormControlConcept: FormControl {
        @Suppress("UNUSED")
        @Facet
        val formatHint: TextInputFormatHint

        enum class TextInputFormatHint(@Suppress("UNUSED") val hint: String) {
            PLAIN(""),
            @Suppress("UNUSED") MONEY("12.30"),
            DATE("31.12.2023"),
        }

    }

    interface SelectDropdownFormControlConcept: FormControl {
        @Suppress("UNUSED")
        @Facet
        val defaultValue: String?

        @Suppress("UNUSED")
        @Facet
        val selectDropdownEntries: List<SelectDropdownEntryConcept>
    }

    interface SelectDropdownEntryConcept {
        @Suppress("UNUSED")
        @Facet
        val value: String

        @Suppress("UNUSED")
        @Facet
        val displayValue: String
    }
}

