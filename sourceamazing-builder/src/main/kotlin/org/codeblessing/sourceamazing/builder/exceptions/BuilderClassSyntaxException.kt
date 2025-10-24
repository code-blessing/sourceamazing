package org.codeblessing.sourceamazing.builder.exceptions

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage

class BuilderClassSyntaxException(clazz: KClass<*>, errorCodeWithMessage: ErrorCodeWithMessage) :
    BuilderSyntaxException(errorCodeWithMessage.appendLine("Builder Class: $clazz"))
