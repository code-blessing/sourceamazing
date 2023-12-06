package org.codeblessing.sourceamazing.api.process.schema.query.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class QueryConcepts(val conceptClasses: Array<KClass<*>>)
