package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.FacetName

fun String.toFacetName(): FacetName {
    return FacetName.of(this)
}
