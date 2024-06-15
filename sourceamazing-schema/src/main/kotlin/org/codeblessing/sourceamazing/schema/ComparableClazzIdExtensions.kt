package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass

fun KClass<*>.toFacetName(): FacetName {
    return FacetName.of(this)
}

fun KClass<*>.toConceptName(): ConceptName {
    return ConceptName.of(this)
}
