package org.codeblessing.sourceamazing.schema.api

import kotlin.reflect.KClass

fun KClass<*>.toConceptName(): ConceptName {
    return ConceptName.of(this)
}

fun ConceptName.randomConceptIdentifier(): ConceptIdentifier {
    return ConceptIdentifier.ofRandom(this)
}
