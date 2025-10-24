package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaConstructor
import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModel
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.SchemaSyntaxException
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazzProperty
import org.codeblessing.sourceamazing.schema.utils.shortName
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.hasMemberExtensionFunctions
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.hasMemberFunctions
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.isAbstract
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.isEnum
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.isOrdinaryInterface
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.isPrivate
import org.codeblessing.sourceamazing.schema.utils.type.KParameterUtil.hasParameterName
import org.codeblessing.sourceamazing.schema.utils.type.KParameterUtil.isUnitTypeParameter
import org.codeblessing.sourceamazing.schema.utils.type.KParameterUtil.isValueParameter
import org.codeblessing.sourceamazing.schema.utils.type.KParameterUtil.isVarargParameter
import org.codeblessing.sourceamazing.schema.utils.type.KPropertyUtil.hasExtensionReceiverParameter
import org.codeblessing.sourceamazing.schema.utils.type.KPropertyUtil.hasFunctionBody
import org.codeblessing.sourceamazing.schema.utils.type.KPropertyUtil.hasReturnType
import org.codeblessing.sourceamazing.schema.utils.type.KPropertyUtil.hasTypeParameter
import org.codeblessing.sourceamazing.schema.utils.type.KPropertyUtil.hasValueParameters
import org.codeblessing.sourceamazing.schema.utils.type.isPrivateConstructor
import org.codeblessing.sourceamazing.schema.utils.type.isRegularClass
import org.codeblessing.sourceamazing.schema.utils.typedeconstruction.TypeToClassDeconstructor

object ClazzKindDeterminer {
    private val prohibitedInternalClasses: List<KClass<*>> =
        listOf(ClazzModelId::class, TypeSafeClazzModel::class, ClazzModel::class)

    fun determineClazzInformation(definitionClass: KClass<*>): TypeSafeClazzKindInformationImpl {
        checkHasNoGenericTypeParameters(definitionClass)
        checkIsNotInternalClass(definitionClass)

        val proxyableInterfaceInfos = proxyableInterfaceInfos(definitionClass)
        val instantiableClassInfos = instantiableClassInfos(definitionClass)

        return TypeSafeClazzKindInformationImpl(
            clazz = definitionClass.toClazz(),
            proxyableInterfaceRejectionReasons = proxyableInterfaceInfos.rejectionReasons,
            instantiableClassRejectionReasons = instantiableClassInfos.rejectionReasons,
            proxyableInterfaceClazzPropertySchemaList = proxyableInterfaceInfos.clazzPropertySchemaList,
            instantiableClassClazzPropertySchemaList = instantiableClassInfos.clazzPropertySchemaList,
        )
    }

    fun checkHasNoGenericTypeParameters(definitionClass: KClass<*>) {
        if (hasGenericTypeParameters(definitionClass)) {
            throw SchemaSyntaxException(
                SchemaErrorCode.CLASS_CANNOT_HAVE_GENERIC_TYPE_PARAMETER.withFormattedMessage(
                    definitionClass.shortName(),
                    definitionClass.typeParameters,
                )
            )
        }
    }

    fun checkIsNotInternalClass(definitionClass: KClass<*>) {
        if (isInternalClass(definitionClass)) {
            throw SchemaSyntaxException(
                SchemaErrorCode.CLAZZ_CAN_NOT_BE_INTERNAL_CLASS.withFormattedMessage(
                    definitionClass.shortName(),
                    prohibitedInternalClasses,
                )
            )
        }
    }

    private fun isInternalClass(definitionClass: KClass<*>): Boolean {
        return definitionClass in prohibitedInternalClasses
    }

    private fun proxyableInterfaceInfos(definitionClass: KClass<*>): ClazzKindInfoCollector {
        val collector = ClazzKindInfoCollector()
        collector.checkInterfaceIsOrdinaryInterface(definitionClass)
        collector.checkInterfaceHasNoMemberFunctions(definitionClass)
        collector.checkInterfaceHasNoMemberExtensionFunctions(definitionClass)
        collector.checkInterfaceHasNoMemberExtensionProperties(definitionClass)
        collector.checkTypeIsNotInternalClasses(definitionClass)

        getProxyableInterfaceClazzPropertyProperties(definitionClass).forEach { clazzPropertyProperty ->
            collector.checkPropertyHasNoValueParameters(clazzPropertyProperty)
            collector.checkPropertyHasNoExtensionReceiverParameter(clazzPropertyProperty)
            collector.checkPropertyHasNoTypeParameter(clazzPropertyProperty)
            collector.checkPropertyHasNoFunctionBody(clazzPropertyProperty)
            collector.checkPropertyHasReturnType(clazzPropertyProperty)
            collector
                .checkType(
                    definitionClass,
                    clazzPropertyProperty.name,
                    clazzPropertyProperty,
                    clazzPropertyProperty.returnType,
                )
                ?.let { clazzPropertySchema -> collector.clazzPropertySchemaList.add(clazzPropertySchema) }
        }

        return collector
    }

    private fun getProxyableInterfaceClazzPropertyProperties(definitionClass: KClass<*>): Collection<KProperty<*>> {
        return definitionClass.memberProperties
    }

    private fun instantiableClassInfos(definitionClass: KClass<*>): ClazzKindInfoCollector {
        val collector = ClazzKindInfoCollector()

        collector.checkIsRegularClass(definitionClass)
        collector.checkIsNotAbstract(definitionClass)
        collector.checkIsNotPrivate(definitionClass)
        collector.checkIsNotEnum(definitionClass)
        collector.checkHasPrimaryConstructor(definitionClass)
        collector.checkPrimaryConstructorIsNotAbstract(definitionClass)
        collector.checkPrimaryConstructorIsNotPrivate(definitionClass)
        collector.checkPrimaryConstructorHasNoTypeParameters(definitionClass)
        collector.checkEveryConstructorParameterIsValueParameter(definitionClass)
        collector.checkEveryConstructorParametersHaveName(definitionClass)
        collector.checkTypeIsNotInternalClasses(definitionClass)

        getInstantiableClassClazzPropertyParameters(definitionClass).forEach { clazzPropertyParameter ->
            collector.checkParameterIsValueParameter(clazzPropertyParameter)
            collector.checkParameterHasName(clazzPropertyParameter)
            collector.checkParameterIsNotVarargParameter(clazzPropertyParameter)
            collector.checkParameterIsNotUnit(clazzPropertyParameter)
            collector
                .checkType(
                    definitionClass,
                    clazzPropertyParameter.name,
                    clazzPropertyParameter,
                    clazzPropertyParameter.type,
                )
                ?.let { clazzPropertySchema -> collector.clazzPropertySchemaList.add(clazzPropertySchema) }
        }

        return collector
    }

    private fun getInstantiableClassClazzPropertyParameters(definitionClass: KClass<*>): List<KParameter> {
        return getPrimaryConstructor(definitionClass)?.parameters ?: emptyList()
    }

    private class ClazzKindInfoCollector {
        val rejectionReasons = mutableListOf<ErrorCodeWithMessage>()
        val clazzPropertySchemaList = mutableListOf<TypeSafeClazzPropertySchema>()

        fun checkIsRegularClass(definitionClass: KClass<*>) {
            if (!definitionClass.isRegularClass) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_MUST_BE_A_REGULAR_CLASS,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkHasPrimaryConstructor(definitionClass: KClass<*>) {
            val primaryConstructor = getPrimaryConstructor(definitionClass)
            if (primaryConstructor == null) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_MUST_HAVE_A_PRIMARY_CONSTRUCTOR,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkPrimaryConstructorIsNotAbstract(definitionClass: KClass<*>) {
            val primaryConstructor = getPrimaryConstructor(definitionClass)
            if (primaryConstructor == null) {
                return
            }

            if (primaryConstructor.isAbstract) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_PRIMARY_CONSTRUCTOR_CAN_NOT_BE_ABSTRACT,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkPrimaryConstructorIsNotPrivate(definitionClass: KClass<*>) {
            val primaryConstructor = getPrimaryConstructor(definitionClass)
            if (primaryConstructor == null) {
                return
            }

            if (primaryConstructor.isPrivateConstructor) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_PRIMARY_CONSTRUCTOR_CAN_NOT_BE_PRIVATE,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkPrimaryConstructorHasNoTypeParameters(definitionClass: KClass<*>) {
            val primaryConstructor = getPrimaryConstructor(definitionClass)
            if (primaryConstructor == null) {
                return
            }

            if (primaryConstructor.typeParameters.isNotEmpty()) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_PRIMARY_CONSTRUCTOR_CAN_NOT_HAVE_TYPE_PARAMETERS,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkEveryConstructorParameterIsValueParameter(definitionClass: KClass<*>) {
            val primaryConstructor = getPrimaryConstructor(definitionClass)
            if (primaryConstructor == null) {
                return
            }

            if (primaryConstructor.parameters.any { it.kind != KParameter.Kind.VALUE }) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_PRIMARY_CONSTRUCTOR_PARAMETERS_MUST_BE_VALUE_PARAMETERS,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkEveryConstructorParametersHaveName(definitionClass: KClass<*>) {
            val primaryConstructor = getPrimaryConstructor(definitionClass)
            if (primaryConstructor == null) {
                return
            }

            if (primaryConstructor.parameters.any { it.name == null }) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_PRIMARY_CONSTRUCTOR_PARAMETERS_MUST_HAVE_A_NAME,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkIsNotEnum(definitionClass: KClass<*>) {
            if (isEnum(definitionClass)) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE_OR_AN_INSTANTIABLE_CLASS,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkIsNotAbstract(definitionClass: KClass<*>) {
            if (isAbstract(definitionClass)) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE_OR_AN_INSTANTIABLE_CLASS,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkIsNotPrivate(definitionClass: KClass<*>) {
            if (isPrivate(definitionClass)) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_CANNOT_BE_PRIVATE,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkInterfaceIsOrdinaryInterface(definitionClass: KClass<*>) {
            if (!isOrdinaryInterface(definitionClass)) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE_OR_AN_INSTANTIABLE_CLASS,
                    definitionClass.shortName(),
                )
            }
        }

        fun checkTypeIsNotInternalClasses(definitionClass: KClass<*>) {
            if (definitionClass in prohibitedInternalClasses) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.CLAZZ_CAN_NOT_BE_INTERNAL_CLASS,
                    definitionClass.shortName(),
                    prohibitedInternalClasses.map { it.shortName() },
                )
            }
        }

        fun checkInterfaceHasNoMemberExtensionFunctions(definitionClass: KClass<*>) {
            if (hasMemberExtensionFunctions(definitionClass)) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.INTERFACE_CANNOT_HAVE_EXTENSION_FUNCTIONS,
                    definitionClass.shortName(),
                    definitionClass.memberExtensionFunctions,
                )
            }
        }

        fun checkInterfaceHasNoMemberFunctions(definitionClass: KClass<*>) {
            if (hasMemberFunctions(definitionClass)) {
                addClazzKindRejectionReason(
                    definitionClass,
                    SchemaErrorCode.INTERFACE_CANNOT_HAVE_MEMBER_FUNCTIONS,
                    definitionClass.shortName(),
                    definitionClass.memberFunctions,
                )
            }
        }

        fun checkInterfaceHasNoMemberExtensionProperties(definitionClass: KClass<*>) {
            val extensionProperty = definitionClass.memberExtensionProperties.firstOrNull()
            if (extensionProperty != null) {
                addClazzKindRejectionReason(definitionClass, SchemaErrorCode.PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE)
            }
        }

        fun checkPropertyHasNoValueParameters(clazzPropertyProperty: KProperty<*>) {
            if (hasValueParameters(clazzPropertyProperty)) {
                addClazzKindRejectionReason(
                    clazzPropertyProperty,
                    SchemaErrorCode.PROPERTY_CAN_NOT_HAVE_VALUE_PARAMS,
                    clazzPropertyProperty.shortName(),
                )
            }
        }

        fun checkPropertyHasNoExtensionReceiverParameter(clazzPropertyProperty: KProperty<*>) {
            if (hasExtensionReceiverParameter(clazzPropertyProperty)) {
                addClazzKindRejectionReason(
                    clazzPropertyProperty,
                    SchemaErrorCode.PROPERTY_HAS_EXTENSION_RECEIVER_PARAM,
                    clazzPropertyProperty.shortName(),
                )
            }
        }

        fun checkPropertyHasNoTypeParameter(clazzPropertyProperty: KProperty<*>) {
            if (hasTypeParameter(clazzPropertyProperty)) {
                addClazzKindRejectionReason(
                    clazzPropertyProperty,
                    SchemaErrorCode.PROPERTY_HAVE_TYPE_PARAMS,
                    clazzPropertyProperty.shortName(),
                    clazzPropertyProperty.typeParameters,
                )
            }
        }

        fun checkPropertyHasNoFunctionBody(clazzPropertyProperty: KProperty<*>) {
            if (hasFunctionBody(clazzPropertyProperty)) {
                addClazzKindRejectionReason(
                    clazzPropertyProperty,
                    SchemaErrorCode.PROPERTY_MUST_BE_ABSTRACT,
                    clazzPropertyProperty.shortName(),
                )
            }
        }

        fun checkPropertyHasReturnType(clazzPropertyProperty: KProperty<*>) {
            if (!hasReturnType(clazzPropertyProperty)) {
                addClazzKindRejectionReason(
                    clazzPropertyProperty,
                    SchemaErrorCode.CLAZZ_PROPERTY_CANNOT_BE_UNIT_TYPE,
                    clazzPropertyProperty.shortName(),
                )
            }
        }

        fun checkParameterIsValueParameter(clazzPropertyParameter: KParameter) {
            if (!isValueParameter(clazzPropertyParameter)) {
                addClazzKindRejectionReason(
                    clazzPropertyParameter,
                    SchemaErrorCode.CONSTRUCTOR_PARAMETER_CAN_ONLY_BE_VALUE_PARAM,
                    clazzPropertyParameter.shortName(),
                )
            }
        }

        fun checkParameterHasName(clazzPropertyParameter: KParameter) {
            if (!hasParameterName(clazzPropertyParameter)) {
                addClazzKindRejectionReason(
                    clazzPropertyParameter,
                    SchemaErrorCode.CONSTRUCTOR_PARAMETER_MUST_BE_A_NAMED_PARAMETER,
                    clazzPropertyParameter.shortName(),
                )
            }
        }

        fun checkParameterIsNotVarargParameter(clazzPropertyParameter: KParameter) {
            if (isVarargParameter(clazzPropertyParameter)) {
                addClazzKindRejectionReason(
                    clazzPropertyParameter,
                    SchemaErrorCode.CONSTRUCTOR_PARAMETER_CAN_NOT_BE_VARARG_PARAM,
                    clazzPropertyParameter.shortName(),
                )
            }
        }

        fun checkParameterIsNotUnit(clazzPropertyParameter: KParameter) {
            if (isUnitTypeParameter(clazzPropertyParameter)) {
                addClazzKindRejectionReason(
                    clazzPropertyParameter,
                    SchemaErrorCode.CLAZZ_PROPERTY_CANNOT_BE_UNIT_TYPE,
                    clazzPropertyParameter.shortName(),
                )
            }
        }

        fun checkType(
            definitionClass: KClass<*>,
            clazzPropertyOrNull: String?,
            enclosingElement: KAnnotatedElement,
            type: KType,
        ): TypeSafeClazzPropertySchema? {
            val typeDeconstruction = TypeToClassDeconstructor.createClazzTypeDeconstruction(type)
            typeDeconstruction.errorCodesWithMessage.forEach { errorCodeWithMessage ->
                addClazzKindRejectionReason(clazzPropertyOrNull ?: "<unknown>", enclosingElement, errorCodeWithMessage)
            }

            return if (
                typeDeconstruction.errorCodesWithMessage.isEmpty() &&
                    typeDeconstruction.typeDeconstructionData != null &&
                    clazzPropertyOrNull != null
            ) {
                ClazzPropertySchemaCreator.createClazzPropertySchema(
                    clazz = definitionClass.toClazz(),
                    classProperty = clazzPropertyOrNull.toClazzProperty(),
                    typeDeconstructionData = typeDeconstruction.typeDeconstructionData,
                )
            } else {
                null
            }
        }

        @Suppress("UNUSED_PARAMETER")
        private fun addClazzKindRejectionReason(
            definitionClass: KClass<*>,
            reasonCode: SchemaErrorCode,
            vararg messageArguments: Any,
        ) {
            val message = reasonCode.format(*messageArguments)
            rejectionReasons.add(ErrorCodeWithMessage(reasonCode, message))
        }

        private fun addClazzKindRejectionReason(
            clazzPropertyProperty: KProperty<*>,
            reasonCode: SchemaErrorCode,
            vararg messageArguments: Any,
        ) {
            val message = "${reasonCode.format(*messageArguments)}\n  Property: $clazzPropertyProperty"
            rejectionReasons.add(ErrorCodeWithMessage(reasonCode, message))
        }

        private fun addClazzKindRejectionReason(
            clazzPropertyProperty: KParameter,
            reasonCode: SchemaErrorCode,
            vararg messageArguments: Any,
        ) {
            val message = "${reasonCode.format(*messageArguments)}\n  Parameter: $clazzPropertyProperty"
            rejectionReasons.add(ErrorCodeWithMessage(reasonCode, message))
        }

        private fun addClazzKindRejectionReason(
            clazzProperty: String,
            enclosingElement: KAnnotatedElement,
            errorCodeWithMessage: ErrorCodeWithMessage,
        ) {
            val message =
                SchemaErrorCode.CLAZZ_PROPERTY_CLAZZ_TYPE_INVALID.format(
                    clazzProperty,
                    errorCodeWithMessage.message,
                    enclosingElement,
                )
            rejectionReasons.add(ErrorCodeWithMessage(errorCodeWithMessage.errorCode, message))
        }
    }

    private fun getPrimaryConstructor(definitionClass: KClass<*>): KFunction<*>? {
        val primaryConstructor = definitionClass.primaryConstructor
        if (primaryConstructor != null) {
            try {
                primaryConstructor.javaConstructor
            } catch (_: Throwable) {
                // ignore and return no constructor
                return null
            }
        }

        return primaryConstructor
    }
}
