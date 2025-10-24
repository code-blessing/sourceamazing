package org.codeblessing.sourceamazing.builder.typesafeapi.adapter

import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.api.BuilderContext
import org.codeblessing.sourceamazing.builder.typesafeapi.TypeSafeBuilderContext

class TypeSafeBuilderContextAdapter(private val typeSafeBuilderContext: TypeSafeBuilderContext) : BuilderContext {
    override fun getClazzModelId(alias: String): Any {
        return typeSafeBuilderContext.getClazzModelId(Alias.Companion.of(alias)).name
    }
}
