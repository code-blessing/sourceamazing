package org.codeblessing.sourceamazing.schema.api

fun String.toConceptIdentifier(): ConceptIdentifier {
    return ConceptIdentifier.of(this)
}
