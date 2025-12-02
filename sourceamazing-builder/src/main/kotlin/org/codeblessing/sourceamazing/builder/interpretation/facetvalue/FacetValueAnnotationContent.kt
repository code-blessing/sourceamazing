package org.codeblessing.sourceamazing.builder.interpretation.facetvalue

import org.codeblessing.sourceamazing.schema.api.schemaaccess.FacetType

open class FacetValueAnnotationContent(
    val base: FacetValueAnnotationBaseData,
    val expectedFacetType: FacetType? = null,
    val value: Any? = null,
)
