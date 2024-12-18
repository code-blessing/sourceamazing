package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Affects whether a facet value is added to the existing facet values
 * or whether the existing facet values are replaced by the new ones.
 *
 */
enum class FacetModificationRule {
    /**
     * Append the provided value or the values
     * to the facet without removing the already
     * existing facet values.
     */
    ADD,
    /**
     * Remove the already existing facet values
     * and replace it with the provided value or
     * values.
     */
    REPLACE
}