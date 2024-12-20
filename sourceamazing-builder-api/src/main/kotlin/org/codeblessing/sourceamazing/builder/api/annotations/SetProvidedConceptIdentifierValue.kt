package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Set a manually defined concept identifier to a concept.
 *
 *
 * The concept is associated with the concept alias [conceptToModifyAlias].
 *
 * The concept alias is created with the [NewConcept] annotation
 * and declared in [NewConcept.declareConceptAlias].
 *
 * To declare a random concept identifier, use
 * the [SetRandomConceptIdentifierValue] annotation instead.
 *
 * @property conceptToModifyAlias
 * The alias of the concept to which this new concept identifier is assigned to.
 * See [NewConcept] for more about aliases.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SetProvidedConceptIdentifierValue(
    val conceptToModifyAlias: String = DEFAULT_CONCEPT_ALIAS
)
