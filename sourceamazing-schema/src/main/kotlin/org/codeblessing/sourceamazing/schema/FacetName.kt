package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

class FacetName private constructor(facet: ClassMirror): ComparableClazzId(facet) {

    companion object {
        fun of(facet: ClassMirror): FacetName {
            return FacetName(facet)
        }
    }
}
