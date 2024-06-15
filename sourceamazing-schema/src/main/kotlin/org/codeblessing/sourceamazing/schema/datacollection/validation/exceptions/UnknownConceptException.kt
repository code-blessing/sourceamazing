package org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions

import org.codeblessing.sourceamazing.schema.DataCollectionErrorCode


class UnknownConceptException(errorCode: DataCollectionErrorCode, vararg arguments: Any)
    : DataValidationException(errorCode, errorCode.format(*arguments))
