package org.codeblessing.sourceamazing.schema

fun String.toFacetName(): FacetName {
    return FacetName.of(this)
}
