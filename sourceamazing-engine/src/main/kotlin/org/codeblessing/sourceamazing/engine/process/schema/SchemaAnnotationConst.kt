package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.annotations.*

object SchemaAnnotationConst {

    val singleInstanceChildConceptAnnotations: List<Class<out Annotation>> = listOf(
        ChildConcept::class.java,
        ChildConceptWithCommonBaseInterface::class.java
    )

    val listInstanceChildConceptAnnotations: List<Class<out Annotation>> = listOf(
        ChildConcepts::class.java,
        ChildConceptsWithCommonBaseInterface::class.java
    )

    val childConceptAnnotations = listOf(
        ChildConcepts::class.java,
        ChildConcept::class.java
    )
    val childConceptWithCommonInterfaceAnnotations = listOf(
        ChildConceptsWithCommonBaseInterface::class.java,
        ChildConceptWithCommonBaseInterface::class.java
    )

    val allChildConceptAnnotations = childConceptAnnotations + childConceptWithCommonInterfaceAnnotations

    val supportedConceptAnnotations: List<Class<out Annotation>> = allChildConceptAnnotations + listOf(
            Facet::class.java,
            ConceptId::class.java,
        )

    val supportedSchemaAnnotations: List<Class<out Annotation>> = listInstanceChildConceptAnnotations

}