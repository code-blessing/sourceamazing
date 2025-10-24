package org.codeblessing.sourceamazing.schema.utils.typedeconstruction

import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage

data class TypeToClassDeconstruction(
    val errorCodesWithMessage: Set<ErrorCodeWithMessage>,
    val typeDeconstructionData: TypeDeconstructionData?,
)
