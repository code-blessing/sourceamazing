package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedRootAlias
import kotlin.reflect.KClass

class RootClassInterpreter(
    val builderClass: KClass<*>,
) {

    fun getRootAliases(): List<Alias> {
        val rootAlias = builderClass.annotations.filterIsInstance<ExpectedRootAlias>().map { it.conceptAlias.toAlias() }
        return rootAlias
    }
}
