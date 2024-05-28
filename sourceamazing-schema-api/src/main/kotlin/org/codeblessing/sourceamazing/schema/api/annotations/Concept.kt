package org.codeblessing.sourceamazing.schema.api.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Concept(val facets: Array<KClass<*>>)
