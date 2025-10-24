package org.codeblessing.sourceamazing.schema.exceptions

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import kotlin.reflect.KProperty

class WrongPropertySyntaxException(property: KProperty<*>, errorCode: SchemaErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, "${errorCode.format(*messageArguments)}\n Function: $property")
