package org.codeblessing.sourceamazing.engine.process.schema.proxy

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.engine.process.conceptgraph.SortedChildrenConceptNodesProvider
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptNode
import java.lang.reflect.Method
import kotlin.reflect.KClass

object SchemaInvocationHandlerHelper {

    fun mapToProxy(method: Method?, conceptNode: SortedChildrenConceptNodesProvider, createProxy: (interfaceClass: KClass<*>, childConceptNode: ConceptNode) -> Any): List<Any> {
        val conceptNamesAndClasses = getChildConceptNamesWithInterfaceClass(method)

        return conceptNode.children(conceptNamesAndClasses.keys)
            .map { childConceptNode -> createProxy(
                getInterfaceClass(conceptNamesAndClasses, childConceptNode), childConceptNode)
        }

    }

    private fun getInterfaceClass(conceptNamesAndClasses: Map<ConceptName, KClass<*>>, conceptNode: ConceptNode): KClass<*> {
        return conceptNamesAndClasses[conceptNode.conceptName]
            ?: throw IllegalStateException("No concept class for concept name '${conceptNode.conceptName}'.")
    }

    fun isSchemaAnnotated(clazz: Class<*>?): Boolean {
        if(clazz == null) {
            return false
        }
        return clazz.getAnnotation(Schema::class.java) != null
    }

    fun isAnnotatedWithChildConcept(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(ChildConcepts::class.java) != null
    }

    fun isAnnotatedWithChildConceptWithCommonBaseInterface(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(ChildConceptsWithCommonBaseInterface::class.java) != null
    }

    private fun getChildInterfaceClass(method: Method?): KClass<*> {
        return if(isAnnotatedWithChildConcept(method)) {
            validatedMethod(method).getAnnotation(ChildConcepts::class.java).conceptClass
        } else if(isAnnotatedWithChildConceptWithCommonBaseInterface(method)) {
            validatedMethod(method).getAnnotation(ChildConceptsWithCommonBaseInterface::class.java).baseInterfaceClass
        } else {
            throw IllegalStateException("Method '$method' must be annotated " +
                    "with ${ChildConcepts::class.java} or ${ChildConceptsWithCommonBaseInterface::class.java} ")
        }

    }

    fun getChildConceptNamesWithInterfaceClass(method: Method?): Map<ConceptName, KClass<*>> {
        return if(isAnnotatedWithChildConcept(method)) {
            val conceptClass = getChildInterfaceClass(method)
            val conceptName = ConceptName.of(conceptClass.java.getAnnotation(Concept::class.java).conceptName)
            mapOf(conceptName to conceptClass)
        } else if(isAnnotatedWithChildConceptWithCommonBaseInterface(method)) {
            validatedMethod(method)
                .getAnnotation(ChildConceptsWithCommonBaseInterface::class.java).conceptClasses
                .associateBy { conceptNameOfClass(it, method) }
        } else {
            throw IllegalStateException("Method '$method' must be annotated " +
                    "with ${ChildConcepts::class.java} or ${ChildConceptsWithCommonBaseInterface::class.java} ")
        }
    }

    private fun conceptNameOfClass(clazz: KClass<*>, method: Method?): ConceptName {
        val conceptAnnotation = clazz.java.getAnnotation(Concept::class.java)
            ?: throw IllegalStateException("Annotation attribute '${ChildConceptsWithCommonBaseInterface::conceptClasses.name}' " +
                    "of annotation ${ChildConcepts::class.java} " +
                    "in method '$method' " +
                    "can only contain interfaces annotated with ${Concept::class.java}.")
        return ConceptName.of(conceptAnnotation.conceptName)
    }

    fun isInputFacetAnnotated(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(Facet::class.java) != null
    }

    fun isConceptIdentifierAnnotated(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(ConceptId::class.java) != null
    }

    fun isParentConceptAnnotated(method: Method?): Boolean {
        return validatedMethod(method).getAnnotation(ParentConcept::class.java) != null
    }

    fun getInputFacetName(method: Method?): FacetName {
        return FacetName.of(validatedMethod(method).getAnnotation(Facet::class.java).facetName)
    }

    fun validatedMethod(method: Method?): Method {
        if(method == null) {
            throw IllegalStateException("Proxy $this can only handle methods, not field invocations.")
        }

        return method

    }
}
