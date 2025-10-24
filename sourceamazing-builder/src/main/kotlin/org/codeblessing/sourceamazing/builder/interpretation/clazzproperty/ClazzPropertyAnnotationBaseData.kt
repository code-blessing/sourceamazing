package org.codeblessing.sourceamazing.builder.interpretation.clazzproperty

import kotlin.reflect.KType
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.api.annotations.ClazzPropertyModification
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty

class ClazzPropertyAnnotationBaseData(
    val methodLocation: MethodLocation,
    val alias: Alias,
    val classProperty: ClassProperty,
    val clazzPropertyModification: ClazzPropertyModification,
    val annotation: Annotation,
    val ignoreNullValue: Boolean,
    val type: KType?,
)
