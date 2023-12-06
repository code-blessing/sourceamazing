package org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithNewBuilder(
    val builderClass: KClass<*>
)
