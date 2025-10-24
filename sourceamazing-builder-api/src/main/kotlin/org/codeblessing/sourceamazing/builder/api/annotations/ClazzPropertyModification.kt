package org.codeblessing.sourceamazing.builder.api.annotations

enum class ClazzPropertyModification {
    /**
     * Append the provided value or the values to the clazzProperty without removing the already existing clazzProperty
     * values.
     */
    ADD,
    /** Remove the already existing clazzProperty values and replace it with the provided value or values. */
    REPLACE,
}
