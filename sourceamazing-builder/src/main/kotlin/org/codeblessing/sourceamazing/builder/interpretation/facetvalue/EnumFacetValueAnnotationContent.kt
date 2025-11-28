package org.codeblessing.sourceamazing.builder.interpretation.facetvalue

import org.codeblessing.sourceamazing.schema.api.FacetType

class EnumFacetValueAnnotationContent(base: FacetValueAnnotationBaseData, val fixedEnumValue: String?) :
    FacetValueAnnotationContent(base, FacetType.TEXT_ENUMERATION)
