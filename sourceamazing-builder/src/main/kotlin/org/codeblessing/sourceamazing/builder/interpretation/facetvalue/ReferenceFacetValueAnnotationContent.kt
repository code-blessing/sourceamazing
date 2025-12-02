package org.codeblessing.sourceamazing.builder.interpretation.facetvalue

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.schemaaccess.FacetType

class ReferenceFacetValueAnnotationContent(
    base: FacetValueAnnotationBaseData,
    value: ConceptIdentifier?,
    val referencedAlias: Alias?,
) : FacetValueAnnotationContent(base, FacetType.REFERENCE, value)
