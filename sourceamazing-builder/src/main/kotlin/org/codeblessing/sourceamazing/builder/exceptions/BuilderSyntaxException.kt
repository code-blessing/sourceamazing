package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import kotlin.reflect.KClass

class BuilderSyntaxException(clazz: KClass<*>, errorCode: BuilderErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Builder Class: $clazz")