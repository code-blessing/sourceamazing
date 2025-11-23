package org.codeblessing.sourceamazing.builder.interpretation.facetvalue

import kotlin.reflect.KClass
import kotlin.reflect.KType
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.schema.api.FacetName

class FacetValueAnnotationBaseData(
    val methodLocation: MethodLocation,
    val alias: Alias,
    val facetName: FacetName,
    val facetModificationRule: FacetModificationRule,
    val annotation: Annotation,
    val ignoreNullValue: Boolean,
    val type: KType?,
    val typeClass: KClass<*>?,
)
