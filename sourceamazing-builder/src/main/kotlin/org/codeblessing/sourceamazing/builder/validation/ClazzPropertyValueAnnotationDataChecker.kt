package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KType
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderCollectionHelper.SUPPORTED_COLLECTION_TYPES
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.FixedClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.SetClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.utils.type.enumValues
import org.codeblessing.sourceamazing.schema.utils.type.isEnum
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeDeconstructionData
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeToClassDeconstructionOptions
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeToClassDeconstructor

class ClazzPropertyValueAnnotationDataChecker(
    private val clazzPropertyValueAnnotationContent: ClazzPropertyValueAnnotationContent,
    schemaAccess: TypeSafeSchemaAccess,
) : ClazzPropertyAnnotationDataChecker(clazzPropertyValueAnnotationContent, schemaAccess) {

    override fun checkIsCompatibleClazzPropertyValue(
        clazzProperty: TypeSafeClazzPropertySchema,
        clazzPropertyValue: Any,
    ) {
        if (!clazzProperty.isCompatibleClazzPropertyValue(clazzPropertyValue)) {
            wrongClazzPropertyValueErrorCodeWithMessage(clazzProperty, clazzPropertyValue)
        }
    }

    private fun wrongClazzPropertyValueErrorCodeWithMessage(
        clazzPropertySchema: TypeSafeClazzPropertySchema,
        clazzPropertyValue: Any,
    ): Nothing {
        val annotationBaseData = clazzPropertyValueAnnotationContent.base
        val annotationText = annotationBaseData.annotation.annotationClass.annotationText()
        val clazzProperty = clazzPropertySchema.classProperty

        val wrongClazzPropertyValueErrorCodeWithMessage =
            if (clazzPropertySchema.clazzPropertyClazz.clazz.isEnum) {
                BuilderErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_VALUE.withFormattedMessage(
                    annotationText,
                    clazzProperty.longText(),
                    clazzPropertyValue,
                    clazzPropertySchema.clazzPropertyClazz.clazz.enumValues,
                )
            } else {
                BuilderErrorCode.WRONG_CLAZZ_PROPERTY_TYPE.withFormattedMessage(
                    annotationText,
                    clazzProperty.longText(),
                    clazzPropertySchema.clazzPropertyClazz.clazz,
                    clazzPropertyValue,
                )
            }
        throw BuilderMethodSyntaxException(
            clazzPropertyValueAnnotationContent.base.methodLocation,
            wrongClazzPropertyValueErrorCodeWithMessage,
        )
    }

    override fun checkIsCompatibleClazzPropertyType(
        clazzPropertySchema: TypeSafeClazzPropertySchema,
        type: KType,
    ): TypeDeconstructionData {
        val options = TypeToClassDeconstructionOptions(allowNullValues = true)
        val typeToClassDeconstruction = TypeToClassDeconstructor.createClazzTypeDeconstruction(type, options)

        val errorCodesWithMessage = typeToClassDeconstruction.errorCodesWithMessage.toMutableSet()

        val typeDeconstructionData = typeToClassDeconstruction.typeDeconstructionData
        if (typeDeconstructionData != null) {
            if (!clazzPropertySchema.isCompatibleClazzClass(typeDeconstructionData.valueClass)) {
                errorCodesWithMessage.add(
                    BuilderErrorCode.CLAZZ_PROPERTY_INCOMPATIBLE_TYPE.withFormattedMessage(
                        clazzPropertySchema.classProperty,
                        clazzPropertySchema.enclosingClazz,
                        clazzPropertySchema.clazzPropertyClazz.clazz,
                        typeDeconstructionData.valueClass,
                    )
                )
            }
        }

        if (errorCodesWithMessage.isNotEmpty() || typeDeconstructionData == null) {
            throwWrongClazzPropertyTypeErrorCodeWithMessage(clazzPropertySchema, errorCodesWithMessage)
        }
        return typeDeconstructionData
    }

    private fun throwWrongClazzPropertyTypeErrorCodeWithMessage(
        clazzPropertySchema: TypeSafeClazzPropertySchema,
        errorCodesWithMessage: Set<ErrorCodeWithMessage>,
    ): Nothing {
        val annotationBaseData = clazzPropertyValueAnnotationContent.base
        val clazzProperty = clazzPropertySchema.classProperty

        val clazzPropertyClazzClass = clazzPropertySchema.clazzPropertyClazz.clazz

        val additionalMessages = errorCodesWithMessage.joinToString(" ") { it.message }
        val validationErrorCodeWithMessage =
            if (clazzPropertySchema.clazzPropertyClazz.clazz.isEnum) {
                BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_CLAZZ_PROPERTY_TYPE.withFormattedMessage(
                    clazzProperty.longText(),
                    clazzPropertyClazzClass.shortText(),
                    SUPPORTED_COLLECTION_TYPES,
                    clazzPropertyClazzClass.enumValues,
                    additionalMessages,
                )
            } else {
                BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_PROPERTY_TYPE.withFormattedMessage(
                    clazzProperty.longText(),
                    clazzPropertyClazzClass.shortText(),
                    SUPPORTED_COLLECTION_TYPES,
                    additionalMessages,
                )
            }

        val validationMessage = validationErrorCodeWithMessage.message
        val overallErrorCodeWithMessage =
            when (clazzPropertyValueAnnotationContent) {
                is FixedClazzPropertyValueAnnotationContent ->
                    BuilderErrorCode.BUILDER_WRONG_FIXED_CLAZZ_PROPERTY_VALUE_ANNOTATION_PARAM.withFormattedMessage(
                        validationMessage
                    )
                is SetClazzPropertyValueAnnotationContent ->
                    BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_VALUE_PARAMETER.withFormattedMessage(
                        validationMessage
                    )
            }

        throw BuilderMethodSyntaxException(
            annotationBaseData.methodLocation,
            overallErrorCodeWithMessage,
            listOf(validationErrorCodeWithMessage),
        )
    }
}
