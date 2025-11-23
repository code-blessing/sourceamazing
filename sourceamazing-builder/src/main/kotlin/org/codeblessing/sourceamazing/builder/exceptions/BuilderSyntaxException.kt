package org.codeblessing.sourceamazing.builder.exceptions

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException

class BuilderSyntaxException(
    clazz: KClass<*>,
    errorCode: BuilderErrorCode,
    vararg messageArguments: Any,
) : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Builder Class: $clazz")
