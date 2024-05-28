package org.codeblessing.sourceamazing.builder.api.annotations

import kotlin.reflect.KClass

/**
 * Add or replace a facet value (or a list of values) on a concept instance.
 *
 *
 * The concept is associated with the concept alias [conceptToModifyAlias].
 *
 * The concept alias is created with the [NewConcept] annotation
 * and declared in [NewConcept.declareConceptAlias].
 *
 * To declare default values for a certain facet, use
 * the following annotations:
 * * [SetFixedBooleanFacetValue] to add or replace a default boolean value.
 * * [SetFixedIntFacetValue] to add or replace a default numeric value.
 * * [SetFixedStringFacetValue] to add or replace a default string facet value.
 * * [SetFixedEnumFacetValue] to add or replace a default enumeration value.
 * * [SetAliasConceptIdentifierReferenceFacetValue] to add or replace a concept identifier
 * of a previously created concept instance (by the [NewConcept] annotation).
 *
 *
 * @property conceptToModifyAlias
 * The alias of the concept to which this new concept identifier is assigned to.
 * See [NewConcept] for more about aliases.
 * @property facetToModify
 * The name of the facet of the concept in [conceptToModifyAlias]
 * @property facetModificationRule Add or replace the facet value
 * See [FacetModificationRule] for more about modification rules.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SetFacetValue(
    val conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    val facetToModify: KClass<*>,
    val facetModificationRule: FacetModificationRule = FacetModificationRule.ADD,
)
