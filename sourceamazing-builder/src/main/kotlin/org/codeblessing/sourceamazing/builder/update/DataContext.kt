package org.codeblessing.sourceamazing.builder.update

import kotlin.reflect.KParameter
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

class DataContext(
    private val functionArguments: Map<KParameter, Any?>,
    private val newClazzModelIds: MutableMap<Alias, ClazzModelId>,
) {
    fun clazzModelIdByAlias(alias: Alias): ClazzModelId {
        return requireNotNull(newClazzModelIds[alias]) { "No clazz found for alias: $alias" }
    }

    fun valueForMethodParameter(methodParameter: KParameter): Any? {
        require(functionArguments.containsKey(methodParameter)) {
            "Key $methodParameter does not exist on map with arguments $functionArguments."
        }
        return functionArguments[methodParameter]
    }
}
