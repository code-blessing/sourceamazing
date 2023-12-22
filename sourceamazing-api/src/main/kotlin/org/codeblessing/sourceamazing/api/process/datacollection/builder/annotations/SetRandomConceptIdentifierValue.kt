package org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations

/**
 * Set a random concept identifier to a concept.
 *
 *
 * The concept is associated with the concept alias [conceptToModifyAlias].
 *
 * The concept alias is created with the [NewConcept] annotation
 * and declared in [NewConcept.declareConceptAlias].
 *
 * To declare a manual concept identifier, use
 * the [SetConceptIdentifierValue] annotation instead.
 *
 * @property conceptToModifyAlias
 * The alias of the concept to which this new concept identifier is assigned to.
 * See [NewConcept] for more about aliases.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SetRandomConceptIdentifierValue(
    val conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS
)
