package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.ComparableClazzId
import kotlin.reflect.KClass

class ConceptName private constructor(concept: KClass<*>): ComparableClazzId(concept) {

    companion object {
        fun of(concept: KClass<*>): ConceptName {
            return ConceptName(concept)
        }
    }
}
