package org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations

import kotlin.reflect.KClass

/**
 * Add or replace a reference facet value with the concept id of another concept instance.
 *
 *
 * The concept and facet to modify is associated with the
 * concept alias [conceptToModifyAlias]->[facetToModify].
 *
 * The concept alias is created with the [NewConcept] annotation
 * and declared in [NewConcept.declareConceptAlias].
 *
 * To declare default values for a certain facet, use
 * the following annotations:
 * * [SetFixedBooleanFacetValue] to add or replace a default boolean value.
 * * [SetFixedNumberFacetValue] to add or replace a default numeric value.
 * * [SetFixedStringFacetValue] to add or replace a default string facet value.
 * * [SetFixedEnumFacetValue] to add or replace a default enumeration value.
 * * [SetAliasConceptIdentifierReferenceFacetValue] to add or replace a concept identifier
 * of a previously created concept instance (by the [NewConcept] annotation).
 *
 * @property conceptToModifyAlias
 * The alias of the concept to which this new concept identifier is assigned to.
 * See [NewConcept] for more about aliases.
 * @property facetToModify
 * The name of the facet of the concept in [conceptToModifyAlias]
 * @property facetModificationRule To add or replace, see [FacetModificationRule]
 * @property referencedConceptAlias The value to add or replace.
 * The value is a concept identifier of a concept instance previously created
 * with [NewConcept].
 * See [NewConcept] for more about aliases.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SetAliasConceptIdentifierReferenceFacetValue(
    val conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS,
    val facetToModify: KClass<*>, // always a reference facet
    val facetModificationRule: FacetModificationRule = FacetModificationRule.ADD,
    val referencedConceptAlias: String,
)
