package org.codeblessing.sourceamazing.engine.process.schema.proxy

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.engine.process.conceptgraph.SortedChildrenConceptNodesProvider
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.engine.process.schema.SchemaAnnotationConst.allChildConceptAnnotations
import org.codeblessing.sourceamazing.engine.process.schema.SchemaAnnotationConst.childConceptAnnotations
import org.codeblessing.sourceamazing.engine.process.schema.SchemaAnnotationConst.childConceptWithCommonInterfaceAnnotations
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper.isMethodAnnotatedWith
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf
import java.lang.reflect.Method
import kotlin.reflect.KClass

object SchemaInvocationHandlerHelper {

    fun mapToListProxy(method: Method, conceptNode: SortedChildrenConceptNodesProvider, createProxy: (interfaceClass: KClass<*>, childConceptNode: ConceptNode) -> Any): List<Any> {
        val conceptNamesAndClasses = getChildConceptNamesWithInterfaceClass(method)

        return conceptNode.children(conceptNamesAndClasses.keys)
            .map { childConceptNode -> createProxy(
                getInterfaceClass(conceptNamesAndClasses, childConceptNode), childConceptNode)
        }
    }

    fun mapToSingleProxy(method: Method, conceptNode: ConceptNode, createProxy: (interfaceClass: KClass<*>, childConceptNode: ConceptNode) -> Any): Any {
        val conceptNamesAndClasses = getChildConceptNamesWithInterfaceClass(method)

        val matchingConceptNodes = conceptNode.children(conceptNamesAndClasses.keys)
        if(matchingConceptNodes.size != 1) {
            throw IllegalStateException("Expected one concept instance for concept name " +
                    "'${conceptNode.conceptName}' (${conceptNode.conceptIdentifier}) but found ${matchingConceptNodes.size}.")
        }
        return matchingConceptNodes
            .map { childConceptNode -> createProxy(
                getInterfaceClass(conceptNamesAndClasses, childConceptNode), childConceptNode)
            }
            .single()
    }

    private fun getInterfaceClass(conceptNamesAndClasses: Map<ConceptName, KClass<*>>, conceptNode: ConceptNode): KClass<*> {
        return conceptNamesAndClasses[conceptNode.conceptName]
            ?: throw IllegalStateException("No concept class for concept name '${conceptNode.conceptName}'.")
    }

    private fun getChildInterfaceClass(method: Method): KClass<*> {
        return if(isMethodAnnotatedWith(method, ChildConcepts::class.java)) {
            method.getAnnotation(ChildConcepts::class.java).conceptClass
        } else if(isMethodAnnotatedWith(method, ChildConcept::class.java)) {
            method.getAnnotation(ChildConcept::class.java).conceptClass
        } else if(isMethodAnnotatedWith(method, ChildConceptsWithCommonBaseInterface::class.java)) {
            method.getAnnotation(ChildConceptsWithCommonBaseInterface::class.java).baseInterfaceClass
        } else if(isMethodAnnotatedWith(method, ChildConceptWithCommonBaseInterface::class.java)) {
            method.getAnnotation(ChildConceptWithCommonBaseInterface::class.java).baseInterfaceClass
        } else {
            throw IllegalStateException("Method '$method' must be annotated " +
                    "with one of those annotations: $allChildConceptAnnotations ")
        }
    }

    private fun getChildConceptClasses(method: Method): List<KClass<*>> {
        return if(isMethodAnnotatedWith(method, ChildConceptsWithCommonBaseInterface::class.java)) {
            method.getAnnotation(ChildConceptsWithCommonBaseInterface::class.java).conceptClasses.toList()
        } else if(isMethodAnnotatedWith(method, ChildConceptWithCommonBaseInterface::class.java)) {
            method.getAnnotation(ChildConceptWithCommonBaseInterface::class.java).conceptClasses.toList()
        } else {
            throw IllegalStateException("Method '$method' must be annotated " +
                    "with one of the following annotations: $childConceptWithCommonInterfaceAnnotations ")
        }
    }

    private fun getChildConceptNamesWithInterfaceClass(method: Method): Map<ConceptName, KClass<*>> {
        return if(isMethodAnnotatedWithExactlyOneOf(method, childConceptAnnotations)) {
            val conceptClass = getChildInterfaceClass(method)
            val conceptName = ConceptName.of(conceptClass.java.getAnnotation(Concept::class.java).conceptName)
            mapOf(conceptName to conceptClass)
        } else if(isMethodAnnotatedWithExactlyOneOf(method, childConceptWithCommonInterfaceAnnotations)) {
            getChildConceptClasses(method).associateBy { conceptNameOfClass(it, method) }
        } else {
            throw IllegalStateException("Method '$method' must be annotated " +
                    "with one of the following annotations: " +
                    "${allChildConceptAnnotations}. ")
        }
    }

    private fun conceptNameOfClass(clazz: KClass<*>, method: Method): ConceptName {
        val conceptAnnotation = clazz.java.getAnnotation(Concept::class.java)
            ?: throw IllegalStateException("Annotation attribute " +
                    "'${ChildConceptsWithCommonBaseInterface::conceptClasses.name}' " +
                    "of annotation ${ChildConceptsWithCommonBaseInterface::class.java} " +
                    "or ${ChildConceptWithCommonBaseInterface::class.java} " +
                    "in method '$method' " +
                    "can only contain interfaces annotated with ${Concept::class.java}.")
        return ConceptName.of(conceptAnnotation.conceptName)
    }

    fun getInputFacetName(method: Method): FacetName {
        return FacetName.of(method.getAnnotation(Facet::class.java).facetName)
    }
}
