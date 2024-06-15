package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface

class ConceptName private constructor(concept: ClassMirrorInterface): ComparableClazzId(concept) {

    companion object {
        fun of(concept: ClassMirrorInterface): ConceptName {
            return ConceptName(concept)
        }
    }
}
