package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.schema.ConceptName
import kotlin.reflect.KFunction

class RecursionDetector {

    private val printTrace: Boolean = false

    private val validatedBuilderClasses: MutableList<InspectedMethod> = mutableListOf()

    data class InspectedMethod(
        val method: KFunction<*>,
        val expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName>
    )

    /**
     * @return is item to process
     */
    fun pushMethodOntoStack(method: KFunction<*>, expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName>) : Boolean {
        val inspectedMethod = InspectedMethod(method, expectedConceptsFromSuperiorMethod)
        val isProcessItem = inspectedMethod !in validatedBuilderClasses
        validatedBuilderClasses.add(inspectedMethod)
        if(printTrace) {
            println("${if(isProcessItem) "Process" else "Skip"} item: $inspectedMethod")
        }
        return isProcessItem
    }

    fun removeLastMethodFromStack() {
        require(validatedBuilderClasses.isNotEmpty()) { "At least one method must be removed from the stack" }
        val inspectedMethod = validatedBuilderClasses.removeLast()
        if(printTrace) {
            println("Remove item: $inspectedMethod")
        }
    }

}