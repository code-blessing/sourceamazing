package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.reflect.KClass

fun assertExceptionWithErrorCode(exceptionClass: KClass<out SyntaxException>, errorCode: ErrorCode, executable: () -> Unit) {
    val exception = assertThrows(exceptionClass.java, executable)
    println("Exception Message: ${exception.message}")
    assertEquals(errorCode, exception.errorCode)
}