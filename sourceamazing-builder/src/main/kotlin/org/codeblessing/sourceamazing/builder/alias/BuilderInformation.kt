package org.codeblessing.sourceamazing.builder.alias

import org.codeblessing.sourceamazing.schema.ConceptName
import kotlin.reflect.KClass

data class BuilderInformation(
    val builderClass: KClass<*>,
    val aliases: Map<Alias, ConceptName>,
)
