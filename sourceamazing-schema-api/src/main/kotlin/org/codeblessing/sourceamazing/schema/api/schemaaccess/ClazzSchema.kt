package org.codeblessing.sourceamazing.schema.api.schemaaccess

import kotlin.reflect.KClass

interface ClazzSchema {
    val clazz: KClass<*>
    val clazzProperties: List<ClazzPropertySchema>
}
