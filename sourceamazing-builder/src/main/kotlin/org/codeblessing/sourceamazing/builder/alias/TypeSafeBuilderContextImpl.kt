package org.codeblessing.sourceamazing.builder.alias

import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.typesafeapi.TypeSafeBuilderContext
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeClazzAndModelId

class TypeSafeBuilderContextImpl(private val aliases: Map<Alias, TypeSafeClazzAndModelId>) : TypeSafeBuilderContext {
    override fun getClazzModelId(alias: Alias): ClazzModelId {
        return aliases[alias]?.clazzModelId ?: throw NoSuchElementException("No such alias: '${alias.name}'")
    }

    override fun onlyWithClazzes(): Map<Alias, Clazz> {
        return aliases.mapValues { it.value.clazz }
    }

    override fun onlyWithClazzModelIds(): Map<Alias, ClazzModelId> {
        return aliases.mapValues { it.value.clazzModelId }
    }
}
