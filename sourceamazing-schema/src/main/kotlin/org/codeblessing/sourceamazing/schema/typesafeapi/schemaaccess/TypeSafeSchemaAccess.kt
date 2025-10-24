package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess

import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz

interface TypeSafeSchemaAccess {
    fun clazzSchemaByClazz(clazz: Clazz): TypeSafeClazzSchema?

    fun rootClazz(): Clazz
}
