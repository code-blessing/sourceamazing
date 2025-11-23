package org.codeblessing.sourceamazing.builder.interpretation.facetvalue

import kotlin.reflect.KType
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier

class ConceptIdentifierAnnotationData(
    val methodLocation: MethodLocation,
    val alias: Alias,
    val annotation: Annotation,
    val ignoreNullValue: Boolean,
    val type: KType,
    val conceptIdentifier: ConceptIdentifier? = null,
)
