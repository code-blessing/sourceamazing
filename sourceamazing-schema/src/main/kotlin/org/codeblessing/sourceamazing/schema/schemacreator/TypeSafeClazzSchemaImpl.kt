package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.ClazzKindInformation
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzSchema

class TypeSafeClazzSchemaImpl(
    override val clazz: Clazz,
    override val clazzProperties: List<TypeSafeClazzPropertySchema>,
    override val clazzKindInformation: ClazzKindInformation,
) : TypeSafeClazzSchema {
    override val classProperties: List<ClassProperty>
        get() = clazzProperties.map { it.classProperty }.toList()

    override fun hasClazzProperty(classProperty: ClassProperty): Boolean {
        return classProperties.contains(classProperty)
    }

    override fun clazzPropertyByName(classProperty: ClassProperty): TypeSafeClazzPropertySchema? {
        return clazzProperties.firstOrNull { it.classProperty == classProperty }
    }
}
