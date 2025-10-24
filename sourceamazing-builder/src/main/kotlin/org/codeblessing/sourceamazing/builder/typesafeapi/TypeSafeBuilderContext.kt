package org.codeblessing.sourceamazing.builder.typesafeapi

import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId

interface TypeSafeBuilderContext {
    fun getClazzModelId(alias: Alias): ClazzModelId

    fun onlyWithClazzes(): Map<Alias, Clazz>

    fun onlyWithClazzModelIds(): Map<Alias, ClazzModelId>
}
