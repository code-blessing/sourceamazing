package org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions

import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import kotlin.reflect.KClass

class WrongClassStructureSyntaxException(
    clazz: KClass<*>,
    errorCode: SchemaErrorCode,
    vararg messageArguments: Any,
) : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Class: $clazz")
