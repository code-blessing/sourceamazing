package org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions

import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException

class WrongFacetSchemaException(errorCode: SchemaErrorCode, vararg messageArguments: Any)
    : SyntaxException(errorCode, errorCode.format(*messageArguments))
