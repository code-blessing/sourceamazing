package org.codeblessing.sourceamazing.schema.api.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class QueryConcepts(val conceptClasses: Array<KClass<*>>)
