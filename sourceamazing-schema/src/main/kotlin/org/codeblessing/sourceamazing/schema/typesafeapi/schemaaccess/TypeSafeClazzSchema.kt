package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess

import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz

interface TypeSafeClazzSchema {
    val clazz: Clazz
    val clazzKindInformation: ClazzKindInformation
    val clazzProperties: List<TypeSafeClazzPropertySchema>
    val classProperties: List<ClassProperty>

    fun hasClazzProperty(classProperty: ClassProperty): Boolean

    fun clazzPropertyByName(classProperty: ClassProperty): TypeSafeClazzPropertySchema?
}
