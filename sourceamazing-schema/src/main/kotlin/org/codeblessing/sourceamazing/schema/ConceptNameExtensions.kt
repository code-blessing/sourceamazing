package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.ConceptName
import kotlin.reflect.KClass

fun KClass<*>.toConceptName(): ConceptName {
    return ConceptName.of(this)
}
