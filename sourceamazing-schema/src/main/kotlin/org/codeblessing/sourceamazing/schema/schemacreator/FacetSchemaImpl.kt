package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface

data class FacetSchemaImpl(
    override val facetName: FacetName,
    override val facetType: FacetType,
    override val minimumOccurrences: Int,
    override val maximumOccurrences: Int,
    override val referencingConcepts: Set<ConceptName>,
    override val enumerationType: ClassMirrorInterface?
) : FacetSchema {
    override val enumerationValues: List<String> = enumerationType?.enumValues ?: emptyList()
}
