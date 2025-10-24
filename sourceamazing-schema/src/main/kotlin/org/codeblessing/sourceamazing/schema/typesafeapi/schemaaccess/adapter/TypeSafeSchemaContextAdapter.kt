package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.adapter

import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModelCollector
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaAccess
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.typesafeadapter.TypeSafeClazzModelCollectorAdapter

data class TypeSafeSchemaContextAdapter(val typeSafeSchemaContext: TypeSafeSchemaContext) : SchemaContext {
    override val schema: SchemaAccess
        get() = TypeSafeSchemaAccessAdapter(typeSafeSchemaContext.schema)

    override val dataCollector: ClazzModelCollector
        get() = TypeSafeClazzModelCollectorAdapter(typeSafeSchemaContext.dataCollector)
}
