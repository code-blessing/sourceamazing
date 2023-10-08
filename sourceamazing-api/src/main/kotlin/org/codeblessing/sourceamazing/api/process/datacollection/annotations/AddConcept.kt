package org.codeblessing.sourceamazing.api.process.datacollection.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AddConcept(val conceptBuilderClazz: KClass<*>)
