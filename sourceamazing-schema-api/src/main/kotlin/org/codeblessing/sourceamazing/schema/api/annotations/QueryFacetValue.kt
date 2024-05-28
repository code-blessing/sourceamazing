package org.codeblessing.sourceamazing.schema.api.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class QueryFacetValue(val facetClass: KClass<*>)
