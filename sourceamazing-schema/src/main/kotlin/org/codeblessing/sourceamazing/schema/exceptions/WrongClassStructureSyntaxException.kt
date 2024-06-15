package org.codeblessing.sourceamazing.schema.exceptions

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import kotlin.reflect.KClass

class WrongClassStructureSyntaxException(clazz: KClass<*>, errorCode: SchemaErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Class: $clazz")