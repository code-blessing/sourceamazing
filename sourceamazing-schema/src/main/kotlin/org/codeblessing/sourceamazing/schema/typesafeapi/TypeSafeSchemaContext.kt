package org.codeblessing.sourceamazing.schema.typesafeapi

import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModelCollector
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

interface TypeSafeSchemaContext {
    val schema: TypeSafeSchemaAccess
    val dataCollector: TypeSafeClazzModelCollector
}
