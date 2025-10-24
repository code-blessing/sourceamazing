package org.codeblessing.sourceamazing.builder.factories

import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzSchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

class NoopTypeSafeSchemaAccess() : TypeSafeSchemaAccess {
    override fun clazzSchemaByClazz(clazz: Clazz): TypeSafeClazzSchema? {
        throw UnsupportedOperationException()
    }

    override fun rootClazz(): Clazz {
        throw UnsupportedOperationException()
    }
}
