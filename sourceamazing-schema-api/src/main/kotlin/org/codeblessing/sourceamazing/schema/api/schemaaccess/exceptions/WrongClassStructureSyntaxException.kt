package org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode

class WrongClassStructureSyntaxException(
    clazz: KClass<*>,
    errorCode: SchemaErrorCode,
    vararg messageArguments: Any,
) : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Class: $clazz")
