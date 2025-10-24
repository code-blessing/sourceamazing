package org.codeblessing.sourceamazing.schema.clazzgraph

import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

class ClazzInstanceGraph(private val clazzInstances: Map<ClazzModelId, ClazzInstance>) {

    @Throws(NoSuchElementException::class)
    fun instanceByClazzModelId(clazzModelId: ClazzModelId): ClazzInstance {
        return clazzInstances[clazzModelId]
            ?: throw NoSuchElementException("No ClazzInstance with id '${clazzModelId.name}'.")
    }
}
