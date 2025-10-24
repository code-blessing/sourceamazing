package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "Add the enumeration value 'value' to the property 'clazzProperty' of the model declared with
 * 'alias'."
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SetFixedEnumValue(
    val alias: String,
    val clazzProperty: String,
    val modification: ClazzPropertyModification = ClazzPropertyModification.ADD,
    val value: String,
)
