package org.codeblessing.sourceamazing.builder.exceptions

import org.codeblessing.sourceamazing.builder.api.BuilderFactory
import org.codeblessing.sourceamazing.builder.api.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.exceptions.ErrorCodeWithMessage

class BuilderFactoryException(builderFactory: BuilderFactory<*, *>, errorCodeWithMessage: ErrorCodeWithMessage) :
    BuilderSyntaxException(
        errorCodeWithMessage.appendLine("${builderFactory.builderClass}/${builderFactory.builderImplementationClass}")
    )
