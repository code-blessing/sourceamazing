package org.codeblessing.sourceamazing.schema

import kotlin.reflect.KClass

class FacetName private constructor(facet: String): ComparableStringId(facet) {

    companion object {
        fun of(facet: KClass<*>): FacetName {
            return FacetName(facet.simpleName!!)
        }

        fun of(facet: String): FacetName {
            return FacetName(facet)
        }

    }
}
