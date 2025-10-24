package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.SchemaSyntaxException
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzSchema

object ClazzSchemaCreator {
    fun createClazzSchema(clazzClass: KClass<*>): TypeSafeClazzSchema {
        val clazzInfo = ClazzKindDeterminer.determineClazzInformation(clazzClass)

        val clazz = clazzInfo.clazz
        checkAdditionallyKnownClazzModelsNotEmptyIfAnnotationAvailable(clazz)
        checkPossibleReferencesHaveNoDuplicates(clazz)

        val clazzProperties =
            clazzInfo.finalClazzPropertySchemaList.onEach {
                ClazzPropertySchemaValidator.validatedClazzPropertySchema(it)
            }

        return TypeSafeClazzSchemaImpl(clazz, clazzProperties, clazzInfo)
    }

    private fun checkAdditionallyKnownClazzModelsNotEmptyIfAnnotationAvailable(clazz: Clazz) {
        clazz.clazz.findAnnotations<AdditionallyKnownClasses>().forEach { annotation ->
            if (annotation.classes.isEmpty()) {
                throw SchemaSyntaxException(
                    SchemaErrorCode.ADDITIONAL_CLAZZS_ANNOTATION_LIST_EMPTY.withFormattedMessage(clazz)
                )
            }
        }
    }

    private fun checkPossibleReferencesHaveNoDuplicates(clazz: Clazz) {
        val allClazzes =
            clazz.clazz
                .findAnnotations<AdditionallyKnownClasses>()
                .flatMap { annotation -> annotation.classes.toList() }
                .toList()

        if (allClazzes.size != allClazzes.toSet().size) {
            throw SchemaSyntaxException(
                SchemaErrorCode.ADDITIONAL_CLAZZS_ANNOTATION_CONTAINS_DUPLICATES.withFormattedMessage(clazz)
            )
        }
    }
}
