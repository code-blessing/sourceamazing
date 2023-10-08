package org.codeblessing.sourceamazing.engine.parameters

import java.util.Properties

object PropertiesToMapConverter {

    fun convertToMap(props: Properties): Map<String, String> {
        return props
            .map { (key: Any, value: Any) -> Pair(asString(key), asString(value)) }
            .toMap()
    }

    private fun asString(value: Any?): String {
        if (value !is String) {
            throw IllegalArgumentException("$value was not of type String.")
        }
        return value
    }

}
