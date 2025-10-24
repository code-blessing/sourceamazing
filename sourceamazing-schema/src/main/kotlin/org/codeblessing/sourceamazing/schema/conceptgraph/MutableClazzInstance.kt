package org.codeblessing.sourceamazing.schema.clazzgraph

import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

class MutableClazzInstance(
    override val sequenceNumber: Int,
    override val clazz: Clazz,
    override val clazzModelId: ClazzModelId,
    override var clazzPropertyValues: MutableMap<ClassProperty, List<Any>> = mutableMapOf(),
) : ClazzInstance, NeighbourInstance {
    override val neighbours: List<NeighbourInstance>
        get() = clazzPropertyValues.values.flatten().filterIsInstance<NeighbourInstance>()
}
