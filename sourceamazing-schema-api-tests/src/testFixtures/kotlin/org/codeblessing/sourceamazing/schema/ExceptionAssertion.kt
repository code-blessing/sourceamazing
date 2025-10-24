package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCode
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows

const val PRINT_EXCEPTION_MESSAGE_FOR_REVIEW = false

inline fun <reified T : SyntaxException> assertExceptionWithErrorCode(
    expectedMainErrorCode: ErrorCode,
    vararg additionalExpectedReasonErrorCodes: ErrorCode,
    noinline executable: () -> Unit,
) {
    val expectedAdditionalReasonErrorCodes = additionalExpectedReasonErrorCodes.toSet()
    val exception = assertThrows(T::class.java, executable)
    if (PRINT_EXCEPTION_MESSAGE_FOR_REVIEW) {
        println(
            "--- Start exception message ${listOf(expectedMainErrorCode) + expectedAdditionalReasonErrorCodes} --------"
        )
        println(exception.message)
        println("--- End exception message --------")
    }

    val actualAdditionalErrorCodes = exception.reasons.map { it.errorCode }
    assertEquals(expectedMainErrorCode, exception.errorCode)

    for (expectedErrorCode in expectedAdditionalReasonErrorCodes) {
        assertEquals(true, expectedErrorCode in actualAdditionalErrorCodes) {
            "Expected error code $expectedErrorCode not in actual error codes ${actualAdditionalErrorCodes}."
        }
    }
}
