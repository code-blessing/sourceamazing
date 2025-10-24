package org.codeblessing.sourceamazing.schema.clazzgraph

import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

interface NeighbourInstance {
    val clazz: Clazz
    val clazzModelId: ClazzModelId
    val neighbours: List<NeighbourInstance>
}
