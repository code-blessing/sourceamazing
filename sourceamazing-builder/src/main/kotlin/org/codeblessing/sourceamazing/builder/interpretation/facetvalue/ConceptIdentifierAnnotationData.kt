package org.codeblessing.sourceamazing.builder.interpretation.facetvalue

import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import kotlin.reflect.KType

class ConceptIdentifierAnnotationData(
    val methodLocation: MethodLocation,
    val alias: Alias,
    val annotation: Annotation,
    val ignoreNullValue: Boolean,
    val type: KType,
    val conceptIdentifier: ConceptIdentifier? = null,
)
