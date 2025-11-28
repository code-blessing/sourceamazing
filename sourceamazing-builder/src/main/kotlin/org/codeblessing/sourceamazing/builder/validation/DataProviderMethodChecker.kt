package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.utils.type.hasAnnotation
import org.codeblessing.sourceamazing.utils.type.returnTypeOrNull
import org.codeblessing.sourceamazing.utils.type.valueParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter

class DataProviderMethodChecker(private val methodToInspect: KFunction<*>, private val methodLocation: MethodLocation) {

    fun checkHasNoExtensionReceiverParameter() {
        if (methodToInspect.extensionReceiverParameter != null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_CAN_NOT_BE_EXTENSION_FUNCTION,
            )
        }
    }

    fun checkHasNoValueParameters() {
        if (methodToInspect.valueParameters().isNotEmpty()) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_HAS_PARAMETERS,
            )
        }
    }

    fun checkHasReturnTypeIfHasAnnotation(annotation: KClass<out Annotation>) {
        if (methodToInspect.hasAnnotation(annotation) && methodToInspect.returnTypeOrNull() == null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_RETURNS_NOTHING,
            )
        }
    }
}
