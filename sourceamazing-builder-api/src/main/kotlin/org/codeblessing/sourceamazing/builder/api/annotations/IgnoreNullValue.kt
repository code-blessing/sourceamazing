package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * To be read like: "If I pass a null value or a collection containing null values, do ignore them and do not throw an
 * exception."
 */
@Target(AnnotationTarget.VALUE_PARAMETER) @Retention(AnnotationRetention.RUNTIME) annotation class IgnoreNullValue
