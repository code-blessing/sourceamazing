package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzModelIdAnnotationData
import org.codeblessing.sourceamazing.schema.utils.type.KTypeKind
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.schema.utils.type.typeKind

class ClazzModelIdAnnotationDataChecker(private val clazzModelIdAnnotationData: ClazzModelIdAnnotationData) {
    private val methodLocation = clazzModelIdAnnotationData.methodLocation

    fun checkNoClazzModelIdAnnotationAndIgnoreNullValueAnnotationTogether() {
        if (clazzModelIdAnnotationData.ignoreNullValue) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_CLAZZ_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION.withFormattedMessage(),
            )
        }
    }

    fun checkClazzModelIdIsOrdinaryClass() {
        checkAndReturnTypeClass()
    }

    fun checkClazzModelIdIsNotNullable() {
        val typeClass = checkAndReturnTypeClass()

        if (typeClass.isValueNullable) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_CLAZZ_IDENTIFIER_TYPE_NO_NULLABLE.withFormattedMessage(),
            )
        }
    }

    private fun checkAndReturnTypeClass(): KTypeClassInformation {
        return getClazzIdTypeClassOrNull()
            ?: throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_WRONG_CLAZZ_IDENTIFIER_TYPE.withFormattedMessage(
                    clazzModelIdAnnotationData.type
                ),
            )
    }

    private fun getClazzIdTypeClassOrNull(): KTypeClassInformation? {
        val clazzIdType = clazzModelIdAnnotationData.type
        val clazzIdTypeKind = clazzIdType.typeKind()
        if (clazzIdTypeKind == KTypeKind.KCLASS) {
            val typeClasses = KTypeUtil.classesInformationFromKType(clazzIdType)
            if (typeClasses.size == 1) {
                return typeClasses.single()
            }
        }
        return null
    }
}
