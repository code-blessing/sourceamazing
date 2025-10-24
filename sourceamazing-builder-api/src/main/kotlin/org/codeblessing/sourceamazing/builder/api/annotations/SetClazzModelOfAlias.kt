package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "Add the instance declared with alias 'referencedAlias' to the property 'clazzProperty' of the model
 * declared with 'alias'."
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SetClazzModelOfAlias(
    val alias: String,
    val clazzProperty: String,
    val clazzPropertyModification: ClazzPropertyModification = ClazzPropertyModification.ADD,
    val referencedAlias: String,
)
