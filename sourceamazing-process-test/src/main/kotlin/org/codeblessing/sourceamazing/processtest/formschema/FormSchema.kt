package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.schema.api.annotations.References

interface FormSchema {
    @Suppress("UNUSED") val forms: List<FormConcept>

    interface FormConcept {
        @Suppress("UNUSED") val formTitle: String

        @Suppress("UNUSED")
        @References([TextInputFormControlConcept::class, SelectDropdownFormControlConcept::class])
        val formControls: List<FormSchema.FormControl>
    }

    interface FormControl {
        @Suppress("UNUSED") val displayName: String

        @Suppress("UNUSED") val valueRequired: Boolean

        @Suppress("UNUSED") val labels: List<String>
    }

    interface TextInputFormControlConcept : FormControl {
        @Suppress("UNUSED") val formatHint: TextInputFormatHint

        enum class TextInputFormatHint(@Suppress("UNUSED") val hint: String) {
            PLAIN(""),
            @Suppress("UNUSED") MONEY("12.30"),
            DATE("31.12.2023"),
        }
    }

    interface SelectDropdownFormControlConcept : FormControl {
        @Suppress("UNUSED") val defaultValue: String?

        @Suppress("UNUSED") val selectDropdownEntries: List<SelectDropdownEntryConcept>
    }

    interface SelectDropdownEntryConcept {
        @Suppress("UNUSED") val value: String

        @Suppress("UNUSED") val displayValue: String
    }
}
