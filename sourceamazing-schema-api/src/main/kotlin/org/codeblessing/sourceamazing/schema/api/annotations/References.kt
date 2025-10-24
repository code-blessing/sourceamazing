package org.codeblessing.sourceamazing.schema.api.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class References(
    val possibleClassesToReference: Array<KClass<*>>,
)
