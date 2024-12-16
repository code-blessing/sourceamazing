package org.codeblessing.sourceamazing.builder.update

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import kotlin.reflect.KParameter

class DataContext(
    private val functionArguments: Map<KParameter, Any?>,
    private val newConceptIds: MutableMap<Alias, ConceptIdentifier>,
) {
    fun conceptIdByAlias(alias: Alias): ConceptIdentifier {
        return requireNotNull(newConceptIds[alias]) {
            "No concept found for alias: $alias"
        }
    }

    fun valueForMethodParameter(methodParameter: KParameter): Any? {
        require(functionArguments.containsKey(methodParameter)) {
            "Key $methodParameter does not exist on map with arguments $functionArguments."
        }
        return functionArguments[methodParameter]
    }
}