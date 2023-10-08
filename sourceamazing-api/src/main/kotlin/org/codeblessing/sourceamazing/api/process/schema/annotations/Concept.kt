package org.codeblessing.sourceamazing.api.process.schema.annotations


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Concept(val conceptName: String, val minOccurrence: Int = 0, val maxOccurrence: Int = Int.MAX_VALUE)
