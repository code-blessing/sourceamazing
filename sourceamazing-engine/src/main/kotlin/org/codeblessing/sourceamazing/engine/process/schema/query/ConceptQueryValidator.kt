package org.codeblessing.sourceamazing.engine.process.schema.query

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConceptId
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryFacet
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.WrongFacetQueryMalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.util.AnnotationUtil
import kotlin.reflect.KClass

object ConceptQueryValidator {
    @Throws(MalformedSchemaException::class)
    fun validateAccessorMethodsOfConceptClass(conceptClass: KClass<*>) {

        val possibleFacetClasses = AnnotationUtil.getAnnotation(conceptClass, Concept::class).facets.toSet()
        conceptClass.java.methods.forEach { method ->
            if(method.parameterCount > 0) {
                throw WrongFacetQueryMalformedSchemaException("The method has arguments/parameters " +
                        "which is not allowed for methods annotated with " +
                        "${QueryConcepts::class.shortText()} or ${QueryConceptId::class.shortText()}. Method: $method")
            }

            if(AnnotationUtil.hasAnnotation(method, QueryFacet::class)) {
                val queryFacetClass = AnnotationUtil.getAnnotation(method, QueryFacet::class).facetClass

                if(!possibleFacetClasses.contains(queryFacetClass)) {
                    throw WrongFacetQueryMalformedSchemaException("The method has a invalid " +
                            "facet class ${queryFacetClass.shortText()}. Valid facet classes " +
                            "are ${possibleFacetClasses.map { it.shortText() }}. Method: $method")
                }
            } else if (!AnnotationUtil.hasAnnotation(method, QueryConceptId::class)) {
                throw WrongFacetQueryMalformedSchemaException("The method is missing " +
                        "one of the annotations ${QueryFacet::class.shortText()} " +
                        "or ${QueryConceptId::class.shortText()}. Method: $method")
            }
        }
    }
}
