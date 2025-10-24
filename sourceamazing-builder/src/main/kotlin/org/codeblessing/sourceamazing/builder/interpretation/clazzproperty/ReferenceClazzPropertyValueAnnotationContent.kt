package org.codeblessing.sourceamazing.builder.interpretation.clazzproperty

import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

class ReferenceClazzPropertyValueAnnotationContent(
    override val base: ClazzPropertyAnnotationBaseData,
    override val value: ClazzModelId?,
    val referencedAlias: Alias?,
) : ClazzPropertyReferenceAnnotationContent
