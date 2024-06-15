package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface

class FacetName private constructor(facet: ClassMirrorInterface): ComparableClazzId(facet) {

    companion object {
        fun of(facet: ClassMirrorInterface): FacetName {
            return FacetName(facet)
        }
    }
}
