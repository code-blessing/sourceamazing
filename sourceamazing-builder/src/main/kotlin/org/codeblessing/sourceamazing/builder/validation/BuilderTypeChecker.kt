package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.utils.type.KTypeKind
import org.codeblessing.sourceamazing.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.utils.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.utils.type.typeKind
import kotlin.reflect.KType

object BuilderTypeChecker {

    fun checkIsClassParameterType(
        type: KType,
        methodLocation: MethodLocation,
        builderErrorCode: BuilderErrorCode,
    ): List<KTypeClassInformation> {
        if (type.typeKind() == KTypeKind.KCLASS) {
            return KTypeUtil.classesInformationFromKType(type)
        }

        val detailDescription =
            when (type.typeKind()) {
                KTypeKind.FUNCTION -> "Type can only be a class but was '${type}'."
                KTypeKind.OTHER_TYPE,
                KTypeKind.TYPE_PARAMETER -> "Type can only be a class but was '${type}'."
                else -> throw IllegalStateException("Type '${type.typeKind()}' not supported.")
            }

        throw BuilderMethodSyntaxException(methodLocation, builderErrorCode, detailDescription)
    }
}
