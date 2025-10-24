package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KType
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzPropertyAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ReferenceClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeDeconstructionData

abstract class ClazzPropertyAnnotationDataChecker(
    private val clazzPropertyAnnotationContent: ClazzPropertyAnnotationContent,
    protected val schemaAccess: TypeSafeSchemaAccess,
) {
    fun checkIsValidClazzPropertyAlias(knownValidAliases: Map<Alias, Clazz>) {
        validateIsValidAlias(clazzPropertyAnnotationContent.base.alias, knownValidAliases)
        if (
            clazzPropertyAnnotationContent is ReferenceClazzPropertyValueAnnotationContent &&
                clazzPropertyAnnotationContent.referencedAlias != null
        ) {
            validateIsValidAlias(clazzPropertyAnnotationContent.referencedAlias, knownValidAliases)
        }
    }

    private fun validateIsValidAlias(alias: Alias, knownClazzAliases: Map<Alias, Clazz>) {
        val annotationBaseData = clazzPropertyAnnotationContent.base

        if (alias !in knownClazzAliases) {
            val annotation = annotationBaseData.annotation

            throw BuilderMethodSyntaxException(
                annotationBaseData.methodLocation,
                BuilderErrorCode.UNKNOWN_ALIAS.withFormattedMessage(
                    alias.name,
                    annotation.annotationClass.annotationText(),
                    knownClazzAliases.keys,
                ),
            )
        }
    }

    fun checkIsKnownClazzProperty(aliases: Map<Alias, Clazz>) {
        checkKnownClazzPropertyAndReturn(aliases)
    }

    private fun checkKnownClazzPropertyAndReturn(knownClazzAliases: Map<Alias, Clazz>): TypeSafeClazzPropertySchema {
        val annotation = clazzPropertyAnnotationContent.base.annotation
        val alias = clazzPropertyAnnotationContent.base.alias
        val clazzProperty = clazzPropertyAnnotationContent.base.classProperty

        val clazz = requireNotNull(knownClazzAliases[alias]) { "Clazz for ${alias.name} does not exist" }
        return schemaAccess.clazzSchemaByClazz(clazz)?.clazzPropertyByName(clazzProperty)
            ?: throw BuilderMethodSyntaxException(
                clazzPropertyAnnotationContent.base.methodLocation,
                BuilderErrorCode.UNKNOWN_CLAZZ_PROPERTY.withFormattedMessage(
                    annotation.annotationClass.annotationText(),
                    clazzProperty.longText(),
                ),
            )
    }

    fun checkClazzPropertyValue(aliases: Map<Alias, Clazz>) {
        val clazzProperty = checkKnownClazzPropertyAndReturn(aliases)
        val clazzPropertyValue = clazzPropertyAnnotationContent.value

        if (clazzPropertyValue == null) {
            return
        }

        // this check only is for FixedValueAnnotations
        checkIsCompatibleClazzPropertyValue(clazzProperty, clazzPropertyValue)
    }

    protected abstract fun checkIsCompatibleClazzPropertyValue(
        clazzProperty: TypeSafeClazzPropertySchema,
        clazzPropertyValue: Any,
    )

    fun checkClazzPropertyValueType(aliases: Map<Alias, Clazz>) {
        val clazzPropertySchema = checkKnownClazzPropertyAndReturn(aliases)
        val type = clazzPropertyAnnotationContent.base.type
        if (type == null) {
            // e.g. for all fixed value/references annotations
            return
        }

        val typeDeconstructionData = checkIsCompatibleClazzPropertyType(clazzPropertySchema, type)
        checkIgnoreNullValueAnnotationUsage(typeDeconstructionData)
    }

    protected abstract fun checkIsCompatibleClazzPropertyType(
        clazzPropertySchema: TypeSafeClazzPropertySchema,
        type: KType,
    ): TypeDeconstructionData

    private fun checkIgnoreNullValueAnnotationUsage(typeDeconstructionData: TypeDeconstructionData) {
        val annotationBaseData = clazzPropertyAnnotationContent.base
        val methodLocation = annotationBaseData.methodLocation

        val isValueNullable = typeDeconstructionData.isValueNullable
        if (clazzPropertyAnnotationContent.base.ignoreNullValue && !isValueNullable) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_IGNORE_NULL_ANNOTATION_WITHOUT_NULLABLE_TYPE.withFormattedMessage(),
            )
        }
        if (!clazzPropertyAnnotationContent.base.ignoreNullValue && isValueNullable) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION.withFormattedMessage(),
            )
        }

        if (
            typeDeconstructionData.isCollectionNullable != null && typeDeconstructionData.isCollectionNullable == true
        ) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE.withFormattedMessage(),
            )
        }
    }
}
