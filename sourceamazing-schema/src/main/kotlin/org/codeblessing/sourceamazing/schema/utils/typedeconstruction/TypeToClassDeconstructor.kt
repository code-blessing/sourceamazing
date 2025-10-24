package org.codeblessing.sourceamazing.schema.utils.typedeconstruction

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.schema.utils.type.KTypeKind
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.schema.utils.type.isAnnotation

object TypeToClassDeconstructor {
    private val supportedCollectionClasses: List<KClass<*>> =
        listOf(List::class, Set::class, Collection::class, Iterable::class, Array::class)

    fun createClazzTypeDeconstruction(
        type: KType,
        options: TypeToClassDeconstructionOptions = TypeToClassDeconstructionOptions.DEFAULT,
    ): TypeToClassDeconstruction {
        val errorCollector = ClazzTypeErrorCollector(options, type)
        return errorCollector.createTypeDeconstructor(type)
    }

    private class ClazzTypeErrorCollector(
        private val options: TypeToClassDeconstructionOptions,
        private val type: KType,
    ) {
        private val errorCodeWithMessages = mutableSetOf<ErrorCodeWithMessage>()

        fun createTypeDeconstructor(type: KType): TypeToClassDeconstruction {
            val valueTypeClasses: List<KTypeClassInformation>? = checkNoInvalidType {
                KTypeUtil.classesInformationFromKType(type)
            }

            if (valueTypeClasses == null || valueTypeClasses.isEmpty()) {
                addErrorCode(SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_IS_NOT_AVAILABLE, type.toString())
                return createTypeDeconstruction()
            }

            checkEveryKTypeMemberIsKClass(valueTypeClasses)
            checkClassIsCollectionOfOrSingleClass(valueTypeClasses)

            val valueTypeCollectionClass = collectionClassInfo(valueTypeClasses)
            val valueTypeValueClass = valueClassInfo(valueTypeClasses)
            checkHasNoGenericTypeParameters(valueTypeValueClass)
            checkTypeIsNotUnit(valueTypeValueClass)
            checkTypeIsNotAnnotation(valueTypeValueClass)

            if (valueTypeCollectionClass != null) {
                checkCollectionClassIsSupportedClass(valueTypeCollectionClass)
                if (!options.allowNullValues) {
                    checkValueClassNotNullableIfCollectionClassAvailable(valueTypeValueClass)
                }
            }
            val typeDeconstructionData =
                TypeDeconstructionData(
                    valueClass = valueTypeValueClass.clazz,
                    collectionClass = valueTypeCollectionClass?.clazz,
                    isValueNullable = valueTypeValueClass.isValueNullable,
                    isCollectionNullable = valueTypeCollectionClass?.isValueNullable,
                )

            return createTypeDeconstruction(typeDeconstructionData)
        }

        private fun createTypeDeconstruction(
            typeDeconstructionData: TypeDeconstructionData? = null
        ): TypeToClassDeconstruction {
            return TypeToClassDeconstruction(errorCodeWithMessages, typeDeconstructionData = typeDeconstructionData)
        }

        private fun <T> checkNoInvalidType(block: () -> T): T? {
            return try {
                block()
            } catch (ex: Exception) {
                addErrorCode(SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_IS_INVALID, type.toString(), ex.message ?: "")
                return null
            }
        }

        fun checkHasNoGenericTypeParameters(typeClass: KTypeClassInformation) {
            if (hasGenericTypeParameters(typeClass.clazz)) {
                addErrorCode(
                    SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_CLASS_CANNOT_HAVE_GENERIC_TYPE_PARAMETER,
                    typeClass.clazz.longText(),
                    typeClass.clazz.typeParameters,
                )
            }
        }

        private fun checkClassIsCollectionOfOrSingleClass(classesInformation: List<KTypeClassInformation>) {
            if (classesInformation.size > 2 || classesInformation.isEmpty()) {
                addErrorCode(
                    SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_IS_INVALID_ONLY_COLLECTION_OR_CLASS,
                    type.toString(),
                    supportedCollectionClasses,
                )
            }
        }

        private fun checkEveryKTypeMemberIsKClass(classesInformation: List<KTypeClassInformation>) {
            classesInformation.forEach { classInformation ->
                if (classInformation.kind != KTypeKind.KCLASS) {
                    addErrorCode(SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_CAN_ONLY_BE_CLASSES, type.toString())
                }
            }
        }

        private fun checkCollectionClassIsSupportedClass(valueTypeCollectionClass: KTypeClassInformation?) {
            if (valueTypeCollectionClass != null) {
                if (
                    supportedCollectionClasses.none {
                        valueTypeCollectionClass.clazz.starProjectedType == it.starProjectedType
                    }
                ) {
                    addErrorCode(
                        SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_IS_WRONG_COLLECTION_CLASS,
                        type.toString(),
                        supportedCollectionClasses,
                        valueTypeCollectionClass.clazz.longText(),
                    )
                }
            }
        }

        private fun checkValueClassNotNullableIfCollectionClassAvailable(valueClass: KTypeClassInformation) {
            if (valueClass.isValueNullable) {
                addErrorCode(
                    SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED,
                    type.toString(),
                )
            }
        }

        private fun checkTypeIsNotUnit(typeClass: KTypeClassInformation) {
            if (typeClass.clazz == Unit::class) {
                addErrorCode(SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_CANNOT_BE_UNIT_TYPE, type.toString())
            }
        }

        private fun checkTypeIsNotAnnotation(typeClass: KTypeClassInformation) {
            if (typeClass.clazz.isAnnotation) {
                addErrorCode(
                    SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_CANNOT_BE_ANNOTATION_TYPE,
                    type.toString(),
                    typeClass.clazz,
                )
            }
        }

        private fun collectionClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation? {
            return if (hasCollection(classesInformation)) classesInformation.first() else null
        }

        private fun valueClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation {
            return if (hasCollection(classesInformation)) classesInformation.last() else classesInformation.first()
        }

        private fun hasCollection(classesInformation: List<KTypeClassInformation>): Boolean {
            return classesInformation.size == 2
        }

        private fun addErrorCode(errorCode: SchemaErrorCode, vararg messageArguments: Any) {
            val errorWithMessage = ErrorCodeWithMessage(errorCode, errorCode.format(*messageArguments))
            errorCodeWithMessages.add(errorWithMessage)
        }
    }

    private fun KClass<*>.longText(): String {
        return java.name
    }
}
