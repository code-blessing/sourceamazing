package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.type.isFromKotlinAnyClass
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

object RelevantMethodFetcher {
    fun ownMemberFunctions(definitionClass: KClass<*>): List<KFunction<*>> {
        return definitionClass.memberFunctions.filterNot { it.isFromKotlinAnyClass() }
    }

}