package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.adapter

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.schemaaccess.ClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema

data class TypeSafeClazzPropertySchemaAdapter(private val typeSafeClazzPropertySchema: TypeSafeClazzPropertySchema) :
    ClazzPropertySchema {
    override val enclosingClazz: KClass<*>
        get() = typeSafeClazzPropertySchema.enclosingClazz.clazz

    override val clazzProperty: String
        get() = typeSafeClazzPropertySchema.classProperty.value

    override val clazzPropertyClazz: KClass<*>
        get() = typeSafeClazzPropertySchema.clazzPropertyClazz.clazz

    override val minimumOccurrences: Int
        get() = typeSafeClazzPropertySchema.minimumOccurrences

    override val maximumOccurrences: Int
        get() = typeSafeClazzPropertySchema.maximumOccurrences
}
