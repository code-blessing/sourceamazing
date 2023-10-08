package org.codeblessing.sourceamazing.api.process.schema.annotations


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Facet(val facetName: String, val mandatory: Boolean = true)
