package org.codeblessing.sourceamazing.schema.clazzgraph

import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

interface ClazzInstance {
    val sequenceNumber: Int
    val clazz: Clazz
    val clazzModelId: ClazzModelId
    val clazzPropertyValues: Map<ClassProperty, List<Any>> // every property has at least an empty list!
}
