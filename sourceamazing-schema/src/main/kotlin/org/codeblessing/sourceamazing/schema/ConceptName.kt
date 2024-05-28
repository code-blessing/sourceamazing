package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass

class ConceptName private constructor(concept: KClass<*>): ComparableClazzId(concept) {

    companion object {
        fun of(concept: KClass<*>): ConceptName {
            return ConceptName(concept)
        }
    }
}
