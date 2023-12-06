package org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations

/**
 * Affects whether a facet value is added to the existing facet values
 * or whether the existing facet values are replaced by the new ones.
 *
 */
enum class FacetModificationRule {
    ADD,
    REPLACE
}