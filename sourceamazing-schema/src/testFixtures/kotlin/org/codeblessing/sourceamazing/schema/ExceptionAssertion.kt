package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.reflect.KClass

private const val PRINT_EXCEPTION_MESSAGE_FOR_REVIEW = false

fun assertExceptionWithErrorCode(exceptionClass: KClass<out SyntaxException>, errorCode: ErrorCode, executable: () -> Unit) {
    val exception = assertThrows(exceptionClass.java, executable)
    if(PRINT_EXCEPTION_MESSAGE_FOR_REVIEW) {
        println("Exception Message: ${exception.message}")
    }
    assertEquals(errorCode, exception.errorCode)
}