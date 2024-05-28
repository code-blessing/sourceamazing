package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaContext

object SchemaContextAccessor {

    fun SchemaContext.toRevealedSchemaContext(): RevealedSchemaContext {
        if(this is RevealedSchemaContext) {
            return this
        }
        throw IllegalArgumentException("SchemaContext $this is not supported.")
    }
}