package org.codeblessing.sourceamazing.schema.conceptgraph

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import kotlin.reflect.KClass

class MutableConceptNode(
    override val sequenceNumber: Int,
    override val conceptName: ConceptName,
    override val conceptIdentifier: ConceptIdentifier,
    override var facetValues: MutableMap<FacetName, List<Any>> = mutableMapOf(),
): ConceptNode
