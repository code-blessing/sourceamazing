package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "Add the boolean value 'value' to the property 'clazzProperty' of the model declared with 'alias'."
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SetFixedBooleanValue(
    val alias: String,
    val clazzProperty: String,
    val modification: ClazzPropertyModification = ClazzPropertyModification.ADD,
    val value: Boolean,
)
