package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode

fun assertBuilderMethodSyntaxException(expectedBuilderErrorCode: BuilderErrorCode, executable: () -> Unit) {
    assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, expectedBuilderErrorCode, executable)
}

fun assertBuilderMethodParameterSyntaxException(expectedBuilderErrorCode: BuilderErrorCode, executable: () -> Unit) {
    assertExceptionWithErrorCode(BuilderMethodParameterSyntaxException::class, expectedBuilderErrorCode, executable)
}

