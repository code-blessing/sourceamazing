package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzSchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

data class TypeSafeSchemaImpl(private val rootClazz: Clazz, private val clazzes: Map<Clazz, TypeSafeClazzSchema>) :
    TypeSafeSchemaAccess {
    override fun clazzSchemaByClazz(clazz: Clazz): TypeSafeClazzSchema? {
        return clazzes[clazz]
    }

    override fun rootClazz(): Clazz {
        return rootClazz
    }

    fun numberOfClazzes(): Int {
        return clazzes.size
    }
}
