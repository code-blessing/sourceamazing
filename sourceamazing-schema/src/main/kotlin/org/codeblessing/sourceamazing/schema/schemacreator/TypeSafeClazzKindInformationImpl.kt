package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.ClazzKind
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.ClazzKindInformation
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema

data class TypeSafeClazzKindInformationImpl(
    val clazz: Clazz,
    private val proxyableInterfaceRejectionReasons: List<ErrorCodeWithMessage>,
    private val instantiableClassRejectionReasons: List<ErrorCodeWithMessage>,
    private val proxyableInterfaceClazzPropertySchemaList: List<TypeSafeClazzPropertySchema>,
    private val instantiableClassClazzPropertySchemaList: List<TypeSafeClazzPropertySchema>,
) : ClazzKindInformation {
    override val clazzKind: ClazzKind =
        when {
            proxyableInterfaceRejectionReasons.isEmpty() -> ClazzKind.PROXYABLE_INTERFACE
            instantiableClassRejectionReasons.isEmpty() -> ClazzKind.LATER_CONSTRUCTIBLE_CLASS
            else -> ClazzKind.ONLY_CONSTRUCTED_INSTANCE
        }

    override val clazzKindReasons: List<ErrorCodeWithMessage> =
        (proxyableInterfaceRejectionReasons + instantiableClassRejectionReasons).distinct()

    val finalClazzPropertySchemaList: List<TypeSafeClazzPropertySchema> =
        when (clazzKind) {
            ClazzKind.PROXYABLE_INTERFACE -> proxyableInterfaceClazzPropertySchemaList
            ClazzKind.LATER_CONSTRUCTIBLE_CLASS -> instantiableClassClazzPropertySchemaList
            ClazzKind.ONLY_CONSTRUCTED_INSTANCE -> emptyList()
        }
}
