package org.codeblessing.sourceamazing.schema.api.schemaaccess

import kotlin.reflect.KClass

interface SchemaAccess {
    fun clazzSchemaByClazz(clazz: KClass<*>): ClazzSchema?
}
