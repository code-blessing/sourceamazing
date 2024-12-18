package org.codeblessing.sourceamazing.builder.interpretation.facetvalue

import org.codeblessing.sourceamazing.schema.FacetType

class EnumFacetValueAnnotationContent(
    base: FacetValueAnnotationBaseData,
    val fixedEnumValue: String?,
    val enumValue: Enum<*>? = null,
): FacetValueAnnotationContent(base, FacetType.TEXT_ENUMERATION)
