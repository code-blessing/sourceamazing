package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.adapter

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ClazzPropertySchema
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ClazzSchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzSchema

data class TypeSafeClazzSchemaAdapter(val typeSafeClazzSchema: TypeSafeClazzSchema) : ClazzSchema {
    override val clazz: KClass<*>
        get() = typeSafeClazzSchema.clazz.clazz

    override val clazzProperties: List<ClazzPropertySchema>
        get() = typeSafeClazzSchema.clazzProperties.map { TypeSafeClazzPropertySchemaAdapter(it) }
}
