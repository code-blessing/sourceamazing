package org.codeblessing.sourceamazing.api.rules

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal class NameEnforcerTest {

    @org.junit.jupiter.api.Test
    fun isValidName() {

        assertThat(Validity.NAME_IS_VALID, "ConceptName")
        assertThat(Validity.NAME_IS_VALID, "Concept123Name")
        assertThat(Validity.NAME_IS_VALID, "CONCEPT123NAME")
        assertThat(Validity.NAME_IS_VALID, "C1")
        assertThat(Validity.NAME_IS_VALID, "Ca")
        assertThat(Validity.NAME_IS_VALID, "CA")

        assertThat(Validity.NAME_IS_NOT_VALID, "")
        assertThat(Validity.NAME_IS_NOT_VALID, "C")
        assertThat(Validity.NAME_IS_NOT_VALID, "concept123Name")
        assertThat(Validity.NAME_IS_NOT_VALID, " Concept123Name")
        assertThat(Validity.NAME_IS_NOT_VALID, "cONCEPT123Name")
        assertThat(Validity.NAME_IS_NOT_VALID, "_ConceptName")
        assertThat(Validity.NAME_IS_NOT_VALID, "Concept_Name")
        assertThat(Validity.NAME_IS_NOT_VALID, "Concept Name")
        assertThat(Validity.NAME_IS_NOT_VALID, "Concept-Name")
        assertThat(Validity.NAME_IS_NOT_VALID, "-ConceptName")
    }

    private fun assertThat(validity: Validity, name: String) {
        when(validity) {
            Validity.NAME_IS_VALID -> assertTrue(
                NameEnforcer.isValidName(
                    name
                ), "Name '$name' was assert to be valid, but was not.")
            Validity.NAME_IS_NOT_VALID -> assertFalse(
                NameEnforcer.isValidName(
                    name
                ), "Name '$name' was assert to be invalid, but was valid.")
        }
    }

    private enum class Validity {
        NAME_IS_VALID, NAME_IS_NOT_VALID
    }
}
