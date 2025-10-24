package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeDeconstructionData

object ClazzPropertySchemaCreator {
    fun createClazzPropertySchema(
        clazz: Clazz,
        classProperty: ClassProperty,
        typeDeconstructionData: TypeDeconstructionData,
    ): TypeSafeClazzPropertySchema {
        return createClazzPropertySchemaImplementation(clazz, classProperty, typeDeconstructionData)
    }

    private fun createClazzPropertySchemaImplementation(
        clazz: Clazz,
        classProperty: ClassProperty,
        typeDeconstructionData: TypeDeconstructionData,
    ): TypeSafeClazzPropertySchema {
        val isCollection = typeDeconstructionData.collectionClass != null
        val isNullable = typeDeconstructionData.isValueNullable
        val clazzPropertyType = typeDeconstructionData.valueClass.toClazz()
        val minimumOccurrences: Int = if (isNullable || isCollection) 0 else 1
        val maximumOccurrences: Int = if (isCollection) Int.MAX_VALUE else 1

        return TypeSafeClazzPropertySchemaImpl(
            enclosingClazz = clazz,
            classProperty = classProperty,
            clazzPropertyClazz = clazzPropertyType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
        )
    }
}
