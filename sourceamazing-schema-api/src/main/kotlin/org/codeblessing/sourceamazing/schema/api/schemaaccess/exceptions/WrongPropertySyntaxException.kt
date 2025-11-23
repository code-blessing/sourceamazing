package org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions

import kotlin.reflect.KProperty
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode

class WrongPropertySyntaxException(
    property: KProperty<*>,
    errorCode: SchemaErrorCode,
    vararg messageArguments: Any,
) : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Function: $property")
