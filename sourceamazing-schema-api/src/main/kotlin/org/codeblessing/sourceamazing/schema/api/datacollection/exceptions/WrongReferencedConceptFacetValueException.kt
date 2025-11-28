package org.codeblessing.sourceamazing.schema.api.datacollection.exceptions

import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode

class WrongReferencedConceptFacetValueException(errorCode: DataCollectionErrorCode, vararg arguments: Any) :
    DataValidationException(errorCode, errorCode.format(*arguments))
