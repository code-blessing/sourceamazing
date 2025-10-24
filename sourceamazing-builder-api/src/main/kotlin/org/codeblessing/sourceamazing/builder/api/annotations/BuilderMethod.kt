package org.codeblessing.sourceamazing.builder.api.annotations

/**
 * Every method on a builder interface that should be implemented dynamically by the builder processor has to be marked
 * with this annotation. Default methods that have a method body do not need to be marked with this annotation.
 */
@Target(AnnotationTarget.FUNCTION) @Retention(AnnotationRetention.RUNTIME) annotation class BuilderMethod
