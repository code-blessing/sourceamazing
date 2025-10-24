package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KFunction
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz

class RecursionDetector {

    private val printTrace: Boolean = false

    private val validatedBuilderClasses: MutableList<InspectedMethod> = mutableListOf()

    data class InspectedMethod(val method: KFunction<*>, val expectedClazzesFromSuperiorMethod: Map<Alias, Clazz>)

    /** @return is item to process */
    fun pushMethodOntoStack(method: KFunction<*>, expectedClazzesFromSuperiorMethod: Map<Alias, Clazz>): Boolean {
        val inspectedMethod = InspectedMethod(method, expectedClazzesFromSuperiorMethod)
        val isProcessItem = inspectedMethod !in validatedBuilderClasses
        validatedBuilderClasses.add(inspectedMethod)
        if (printTrace) {
            println("${if(isProcessItem) "Process" else "Skip"} item: $inspectedMethod")
        }
        return isProcessItem
    }

    fun removeLastMethodFromStack() {
        require(validatedBuilderClasses.isNotEmpty()) { "At least one method must be removed from the stack" }
        val inspectedMethod = validatedBuilderClasses.removeLast()
        if (printTrace) {
            println("Remove item: $inspectedMethod")
        }
    }
}
