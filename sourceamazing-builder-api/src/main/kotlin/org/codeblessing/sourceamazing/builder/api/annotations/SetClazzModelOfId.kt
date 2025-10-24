package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "Use the passed value as a clazz identifier to find a clazz instance and add this instance to the
 * property 'clazzProperty' of the clazz model declared with 'alias'." (The passed value is the method argument of the
 * method parameter that is annotated by this annotation)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SetClazzModelOfId(
    val alias: String,
    val clazzProperty: String,
    val modification: ClazzPropertyModification = ClazzPropertyModification.ADD,
)
