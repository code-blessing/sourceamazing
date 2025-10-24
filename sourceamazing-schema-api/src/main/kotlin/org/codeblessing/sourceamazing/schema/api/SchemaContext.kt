package org.codeblessing.sourceamazing.schema.api

import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModelCollector
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaAccess

interface SchemaContext {
    val schema: SchemaAccess
    val dataCollector: ClazzModelCollector
}
