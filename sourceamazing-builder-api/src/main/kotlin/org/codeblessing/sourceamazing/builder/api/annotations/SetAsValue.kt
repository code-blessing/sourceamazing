package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "Add the passed value to the property 'clazzProperty' of the model declared with 'alias'." (The
 * passed value is the method argument of the method parameter that is annotated by this annotation)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SetAsValue(
    val alias: String,
    val clazzProperty: String,
    val modification: ClazzPropertyModification = ClazzPropertyModification.ADD,
)
