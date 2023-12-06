package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.ComparableClazzId
import kotlin.reflect.KClass

class FacetName private constructor(facet: KClass<*>): ComparableClazzId(facet) {

    companion object {
        fun of(facet: KClass<*>): FacetName {
            return FacetName(facet)
        }
    }
}
