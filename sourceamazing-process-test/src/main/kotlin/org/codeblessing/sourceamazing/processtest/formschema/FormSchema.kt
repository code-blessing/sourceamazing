package org.codeblessing.sourceamazing.processtest.formschema

import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses

@Suppress("UNUSED")
interface FormSchema {
    val forms: List<FormClazz>

    @AdditionallyKnownClasses([TextInputFormControlClazz::class, SelectDropdownFormControlClazz::class])
    interface FormClazz {
        val formTitle: String

        @Suppress("UNUSED") val formControls: List<FormControl>
    }

    interface FormControl {
        val displayName: String

        val valueRequired: Boolean

        val labels: List<String>
    }

    interface TextInputFormControlClazz : FormControl {
        val formatHint: TextInputFormatHint

        enum class TextInputFormatHint(val hint: String) {
            PLAIN(""),
            MONEY("12.30"),
            DATE("31.12.2023"),
        }
    }

    interface SelectDropdownFormControlClazz : FormControl {
        val defaultValue: String?

        val selectDropdownEntries: List<SelectDropdownEntryClazz>
    }

    interface SelectDropdownEntryClazz {
        val value: String

        val displayValue: String
    }
}
