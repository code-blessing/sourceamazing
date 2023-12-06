package org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations

import kotlin.reflect.KClass

/**
 * Add or replace a facet value (or a list of values) on a concept instance.
 *
 *
 * The concept is associated with the concept alias [conceptToModifyAlias].
 * For more about concept aliases, see [NewConcept].
 *
 * @property conceptToModifyAlias
 * The alias of the concept to which this new concept identifier is assigned to.
 * See [NewConcept] for more about aliases.
 * @property facetToModify
 * The name of the facet of the concept in [conceptToModifyAlias]
 * @property facetModificationRule Add or replace the facet value
 * See [FacetModificationRule] for more about modification rules.
 * @property value
 * The value to set or add.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SetFixedNumberFacetValue(
    val conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    val facetToModify: KClass<*>,
    val facetModificationRule: FacetModificationRule = FacetModificationRule.ADD,
    val value: Int,
)
