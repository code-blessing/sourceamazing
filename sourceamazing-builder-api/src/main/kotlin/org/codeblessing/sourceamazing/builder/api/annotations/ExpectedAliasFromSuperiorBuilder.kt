package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Tells the validator that this builder imports an alias
 * from its parent/calling builder.

 * @property conceptAlias
 * The alias of the concept to which this new concept identifier is assigned to.
 * See [NewConcept] for more about aliases.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class ExpectedAliasFromSuperiorBuilder(
    val conceptAlias: String = DEFAULT_CONCEPT_ALIAS,
)
