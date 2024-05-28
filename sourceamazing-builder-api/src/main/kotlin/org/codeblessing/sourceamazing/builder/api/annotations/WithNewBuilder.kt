package org.codeblessing.sourceamazing.builder.api.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithNewBuilder(
    val builderClass: KClass<*>
)
