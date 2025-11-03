package org.codeblessing.sourceamazing.schema.util

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import java.util.*

// TODO maybe move to schema api
object ConceptIdentifierUtil {

    fun random(conceptName: ConceptName): ConceptIdentifier {
        val uuid = UUID.randomUUID().toString()
        return ConceptIdentifier.of("${conceptName.simpleName()}-Generated-$uuid")
    }
}
