package org.codeblessing.sourceamazing.builder.interpretation

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz

class BuilderClassInterpreter(
    val builderClass: KClass<*>,
    private val newClazzesWithAliasFromSuperiorBuilder: Map<Alias, Clazz>,
) {

    fun expectedAliasesFromSuperiorBuilderIncludingDuplicates(): List<Alias> {
        return builderClass.annotations.filterIsInstance<ExpectedClazzModelFromSuperiorBuilder>().map {
            it.alias.toAlias()
        }
    }

    fun expectedAliasesFromSuperiorBuilder(): Set<Alias> {
        return expectedAliasesFromSuperiorBuilderIncludingDuplicates().toSet()
    }

    private fun newClazzesFromSuperiorBuilder(): Map<Alias, Clazz> {
        return newClazzesWithAliasFromSuperiorBuilder
    }

    fun newClazzAliasesFromSuperiorBuilder(): Set<Alias> {
        return newClazzesFromSuperiorBuilder().keys
    }

    fun expectedAliasesAndClazzesFromSuperiorBuilder(): Map<Alias, Clazz> {
        return builderClass.annotations.filterIsInstance<ExpectedClazzModelFromSuperiorBuilder>().associate {
            Pair(it.alias.toAlias(), it.clazz.toClazz())
        }
    }

    fun newClazzesFromSuperiorBuilderFilteredByExpectedAliases(): Map<Alias, Clazz> {
        val expectedAliases = expectedAliasesFromSuperiorBuilder()
        return newClazzesFromSuperiorBuilder().filterKeys { key -> key in expectedAliases }
    }
}
