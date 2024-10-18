package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import kotlin.reflect.KClass

fun assertSyntaxException(expectedExceptionClass: KClass<out SyntaxException>, expectedSchemaErrorCode: SchemaErrorCode, executable: () -> Unit) {
    assertExceptionWithErrorCode(expectedExceptionClass, expectedSchemaErrorCode, executable)
}

