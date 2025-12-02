package org.codeblessing.sourceamazing.utils

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import org.codeblessing.sourceamazing.utils.type.isFromKotlinAnyClass

object RelevantMethodFetcher {
    fun ownMemberFunctions(definitionClass: KClass<*>): List<KFunction<*>> {
        return filterOwnFunctions(definitionClass.memberFunctions)
    }

    fun filterOwnFunctions(functions: Collection<KFunction<*>>): List<KFunction<*>> {
        return functions.filterNot { it.isFromKotlinAnyClass() }
    }
}
