package org.codeblessing.sourceamazing.api.process.schema.query.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class QueryFacet(val facetClass: KClass<*>)
