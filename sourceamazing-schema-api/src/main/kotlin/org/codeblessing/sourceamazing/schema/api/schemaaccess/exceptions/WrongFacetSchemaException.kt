package org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions

import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode

class WrongFacetSchemaException(errorCode: SchemaErrorCode, vararg messageArguments: Any) :
    SyntaxException(errorCode, errorCode.format(*messageArguments))
