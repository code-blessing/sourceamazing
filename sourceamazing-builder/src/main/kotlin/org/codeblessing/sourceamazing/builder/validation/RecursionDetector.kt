package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.schema.ConceptName
import kotlin.reflect.KFunction

class RecursionDetector {

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
        if(inspectedMethod in validatedBuilderClasses) {
            return false
        }
        validatedBuilderClasses.add(inspectedMethod)
        return true
    }

    fun removeLastMethodFromStack() {
        require(validatedBuilderClasses.isNotEmpty()) { "At least one method must be removed from the stack" }
        validatedBuilderClasses.removeLast()
    }

}