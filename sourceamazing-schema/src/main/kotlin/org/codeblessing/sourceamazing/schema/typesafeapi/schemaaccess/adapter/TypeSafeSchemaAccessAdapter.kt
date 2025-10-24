package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.adapter

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ClazzSchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaAccess
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz

data class TypeSafeSchemaAccessAdapter(private val typeSafeSchemaAccess: TypeSafeSchemaAccess) : SchemaAccess {
    override fun clazzSchemaByClazz(clazz: KClass<*>): ClazzSchema? {
        return typeSafeSchemaAccess.clazzSchemaByClazz(clazz.toClazz())?.let { TypeSafeClazzSchemaAdapter(it) }
    }
}
