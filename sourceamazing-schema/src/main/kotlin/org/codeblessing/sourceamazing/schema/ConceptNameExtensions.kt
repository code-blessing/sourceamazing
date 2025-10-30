package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass

fun KClass<*>.toConceptName(): ConceptName {
    return ConceptName.of(this)
}
