package org.codeblessing.sourceamazing.builder.factories

import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModelCollector
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

class NoopTypeSafeSchemaContext() : TypeSafeSchemaContext {
    override val schema: TypeSafeSchemaAccess = NoopTypeSafeSchemaAccess()
    override val dataCollector: TypeSafeClazzModelCollector = NoopTypeSafeClazzModelCollector()
}
