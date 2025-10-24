package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KType
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderCollectionHelper.SUPPORTED_COLLECTION_TYPES
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzPropertyReferenceAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ReferenceClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.SetClazzPropertyReferenceAnnotationContent
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.utils.type.enumValues
import org.codeblessing.sourceamazing.schema.utils.type.isEnum
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeDeconstructionData
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeToClassDeconstructionOptions
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeToClassDeconstructor

class ClazzPropertyReferenceAnnotationDataChecker(
    private val clazzPropertyReferenceAnnotationContent: ClazzPropertyReferenceAnnotationContent,
    schemaAccess: TypeSafeSchemaAccess,
) : ClazzPropertyAnnotationDataChecker(clazzPropertyReferenceAnnotationContent, schemaAccess) {

    override fun checkIsCompatibleClazzPropertyValue(
        clazzProperty: TypeSafeClazzPropertySchema,
        clazzPropertyValue: Any,
    ) {
        if (!clazzProperty.isCompatibleClazzPropertyReference(clazzPropertyValue)) {
            throwWrongClazzPropertyValueErrorCodeWithMessage(clazzProperty, clazzPropertyValue)
        }
    }

    private fun throwWrongClazzPropertyValueErrorCodeWithMessage(
        clazzPropertySchema: TypeSafeClazzPropertySchema,
        clazzPropertyValue: Any,
    ): Nothing {
        val annotationBaseData = clazzPropertyReferenceAnnotationContent.base
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
            clazzPropertyReferenceAnnotationContent.base.methodLocation,
            wrongClazzPropertyValueErrorCodeWithMessage,
        )
    }

    override fun checkIsCompatibleClazzPropertyType(
        clazzPropertySchema: TypeSafeClazzPropertySchema,
        type: KType,
    ): TypeDeconstructionData {
        val options = TypeToClassDeconstructionOptions(allowNullValues = true)
        val typeToClassDeconstruction = TypeToClassDeconstructor.createClazzTypeDeconstruction(type, options)
        val typeDeconstructionData = typeToClassDeconstruction.typeDeconstructionData

        val errorCodesWithMessage = typeToClassDeconstruction.errorCodesWithMessage.toMutableSet()

        if (typeDeconstructionData != null) {
            if (!clazzPropertySchema.isCompatibleClazzPropertyReference(typeDeconstructionData.valueClass)) {
                errorCodesWithMessage.add(
                    BuilderErrorCode.CLAZZ_PROPERTY_INCOMPATIBLE_REFERENCE_TYPE.withFormattedMessage(
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
        val annotationBaseData = clazzPropertyReferenceAnnotationContent.base
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
            when (clazzPropertyReferenceAnnotationContent) {
                is ReferenceClazzPropertyValueAnnotationContent ->
                    BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_VALUE_PARAM.withFormattedMessage(validationMessage)
                is SetClazzPropertyReferenceAnnotationContent ->
                    BuilderErrorCode.BUILDER_PARAM_WRONG_SET_CLAZZ_PROPERTY_REFERENCE_PARAMETER.withFormattedMessage(
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
