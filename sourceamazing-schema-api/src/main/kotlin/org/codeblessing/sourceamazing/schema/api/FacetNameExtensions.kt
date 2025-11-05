package org.codeblessing.sourceamazing.schema.api

fun String.toFacetName(): FacetName {
    return FacetName.of(this)
}
