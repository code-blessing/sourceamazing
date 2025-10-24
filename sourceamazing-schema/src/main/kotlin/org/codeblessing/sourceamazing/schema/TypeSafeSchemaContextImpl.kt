package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.datacollection.TypeSafeClazzModelCollectorImpl
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

class TypeSafeSchemaContextImpl(
    override val schema: TypeSafeSchemaAccess,
    override val dataCollector: TypeSafeClazzModelCollectorImpl,
) : TypeSafeSchemaContext
