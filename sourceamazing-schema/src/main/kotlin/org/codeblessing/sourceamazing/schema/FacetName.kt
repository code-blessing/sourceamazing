package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass

class FacetName private constructor(facet: KClass<*>): ComparableClazzId(facet) {

    companion object {
        fun of(facet: KClass<*>): FacetName {
            return FacetName(facet)
        }
    }
}
