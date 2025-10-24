package org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage

interface ClazzKindInformation {
    val clazzKind: ClazzKind

    val clazzKindReasons: List<ErrorCodeWithMessage>
}
