package org.codeblessing.sourceamazing.api.process.schema.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Concept(val facets: Array<KClass<*>>)
