package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.SchemaSyntaxException
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.utils.type.isEnum
import org.codeblessing.sourceamazing.schema.utils.type.isPrivate

object ClazzPropertySchemaValidator {
    fun validatedClazzPropertySchema(clazzPropertySchema: TypeSafeClazzPropertySchema) {
        checkIsNotPrivateEnum(clazzPropertySchema)
        checkMinimumAndMaximumOccurrencesNotNegative(clazzPropertySchema)
        checkMinimumOccurrencesLowerOrEqualToMaximumOccurrences(clazzPropertySchema)
    }

    private fun checkIsNotPrivateEnum(clazzPropertySchema: TypeSafeClazzPropertySchema) {
        val clazzPropertyClazz = clazzPropertySchema.clazzPropertyClazz.clazz
        if (clazzPropertyClazz.isEnum && clazzPropertyClazz.isPrivate) {
            throw SchemaSyntaxException(
                SchemaErrorCode.CLAZZ_PROPERTY_ENUM_HAS_PRIVATE_MODIFIER.withFormattedMessage(
                    clazzPropertySchema.classProperty,
                    clazzPropertySchema.enclosingClazz,
                    clazzPropertyClazz,
                )
            )
        }
    }

    private fun checkMinimumOccurrencesLowerOrEqualToMaximumOccurrences(
        clazzPropertySchema: TypeSafeClazzPropertySchema
    ) {
        val minimumOccurrences = clazzPropertySchema.minimumOccurrences
        val maximumOccurrences = clazzPropertySchema.maximumOccurrences

        if (minimumOccurrences > maximumOccurrences) {
            throw SchemaSyntaxException(
                SchemaErrorCode.CLAZZ_PROPERTY_HAS_WRONG_CLAZZ_PROPERTY_CARDINALITIES.withFormattedMessage(
                    clazzPropertySchema.classProperty,
                    clazzPropertySchema.enclosingClazz,
                    minimumOccurrences,
                    maximumOccurrences,
                )
            )
        }
    }

    private fun checkMinimumAndMaximumOccurrencesNotNegative(clazzPropertySchema: TypeSafeClazzPropertySchema) {
        val minimumOccurrences = clazzPropertySchema.minimumOccurrences
        val maximumOccurrences = clazzPropertySchema.maximumOccurrences

        if (minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw SchemaSyntaxException(
                SchemaErrorCode.CLAZZ_PROPERTY_CANNOT_HAVE_NEGATIVE_CLAZZ_PROPERTY_CARDINALITIES.withFormattedMessage(
                    clazzPropertySchema.classProperty,
                    clazzPropertySchema.enclosingClazz,
                    minimumOccurrences,
                    maximumOccurrences,
                )
            )
        }
    }
}
