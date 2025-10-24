package org.codeblessing.sourceamazing.builder.interpretation.clazzproperty

import kotlin.reflect.KType
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

class ClazzModelIdAnnotationData(
    val methodLocation: MethodLocation,
    val alias: Alias,
    val annotation: Annotation,
    val ignoreNullValue: Boolean,
    val type: KType,
    val clazzModelId: ClazzModelId? = null,
)
