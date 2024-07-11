package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

class ConceptName private constructor(concept: ClassMirror): ComparableClazzId(concept) {

    companion object {
        fun of(concept: ClassMirror): ConceptName {
            return ConceptName(concept)
        }
    }
}
