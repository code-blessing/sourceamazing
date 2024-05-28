package org.codeblessing.sourceamazing.schema.api.rules

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NameEnforcerTest {

    @Test
    fun isValidIdentifier() {

        assertThat(Validity.IDENTIFIER_IS_VALID, "ConceptName")
        assertThat(Validity.IDENTIFIER_IS_VALID, "Concept123Name")
        assertThat(Validity.IDENTIFIER_IS_VALID, "CONCEPT123NAME")
        assertThat(Validity.IDENTIFIER_IS_VALID, "C1")
        assertThat(Validity.IDENTIFIER_IS_VALID, "Ca")
        assertThat(Validity.IDENTIFIER_IS_VALID, "CA")
        assertThat(Validity.IDENTIFIER_IS_VALID, "Concept_Name")
        assertThat(Validity.IDENTIFIER_IS_VALID, "Concept-Name")

        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, "")
        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, "C")
        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, "concept123Name")
        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, " Concept123Name")
        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, "cONCEPT123Name")
        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, "_ConceptName")
        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, "Concept Name")
        assertThat(Validity.IDENTIFIER_IS_NOT_VALID, "-ConceptName")
    }

    private fun assertThat(validity: Validity, identifier: String) {
        when(validity) {
            Validity.IDENTIFIER_IS_VALID -> Assertions.assertTrue(
                NameEnforcer.isValidIdentifier(
                    identifier
                ), "Concept identifier '$identifier' was assert to be valid, but was not."
            )
            Validity.IDENTIFIER_IS_NOT_VALID -> Assertions.assertFalse(
                NameEnforcer.isValidIdentifier(
                    identifier
                ), "Concept identifier '$identifier' was assert to be invalid, but was valid."
            )
        }
    }

    private enum class Validity {
        IDENTIFIER_IS_VALID, IDENTIFIER_IS_NOT_VALID
    }
}