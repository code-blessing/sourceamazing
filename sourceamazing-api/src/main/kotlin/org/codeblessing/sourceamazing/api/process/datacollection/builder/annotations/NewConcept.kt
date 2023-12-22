package org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations

import kotlin.reflect.KClass

/**
 * Creates a new instance of a concept of type [concept].
 *
 * The created concept is accessible by other annotations
 * over the concept alias declared with [declareConceptAlias].
 *
 * The following annotations access the concept instance with
 * help of the [declareConceptAlias]:
 *
 * To set the concept identifier of a concept instance:
 *   * [SetRandomConceptIdentifierValue]
 *   * [SetConceptIdentifierValue]
 *
 * To change facet values of a concept of a concept instance:
 *   * [SetAliasConceptIdentifierReferenceFacetValue]
 *   * [SetFacetValue]
 *   * [SetFixedEnumFacetValue], [SetFixedBooleanFacetValue],
 * [SetFixedIntFacetValue], [SetFixedStringFacetValue],
 * [SetFixedEnumFacetValue], [SetAliasConceptIdentifierReferenceFacetValue]
 *
 * If you handle in a method only one new concept instance,
 * you can leave away the concept alias, a default concept
 * alias will then be used, see [DEFAULT_CONCEPT_ALIAS].
 *
 * @property concept
 *  The type of concept to create.
 * @property declareConceptAlias
 * Arbitrary name (alias,shortcut) to point to this created concept instance in other annotations.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class NewConcept(
    val concept: KClass<*>,
    val declareConceptAlias: String = DEFAULT_CONCEPT_ALIAS
)
