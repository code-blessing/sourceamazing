package org.codeblessing.sourceamazing.schema.api.schemaaccess

import kotlin.reflect.KClass

interface ClazzPropertySchema {
    val enclosingClazz: KClass<*>
    val clazzProperty: String
    val clazzPropertyClazz: KClass<*>
    val minimumOccurrences: Int
    val maximumOccurrences: Int
}
