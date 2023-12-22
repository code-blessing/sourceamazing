package org.codeblessing.sourceamazing.engine.process.schema.query

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryFacetValue
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
                        "${QueryConcepts::class.shortText()} or ${QueryConceptIdentifierValue::class.shortText()}. Method: $method")
            }

            if(AnnotationUtil.hasAnnotation(method, QueryFacetValue::class)) {
                val queryFacetValueClass = AnnotationUtil.getAnnotation(method, QueryFacetValue::class).facetClass

                if(!possibleFacetClasses.contains(queryFacetValueClass)) {
                    throw WrongFacetQueryMalformedSchemaException("The method has a invalid " +
                            "facet class ${queryFacetValueClass.shortText()}. Valid facet classes " +
                            "are ${possibleFacetClasses.map { it.shortText() }}. Method: $method")
                }
            } else if (!AnnotationUtil.hasAnnotation(method, QueryConceptIdentifierValue::class)) {
                throw WrongFacetQueryMalformedSchemaException("The method is missing " +
                        "one of the annotations ${QueryFacetValue::class.shortText()} " +
                        "or ${QueryConceptIdentifierValue::class.shortText()}. Method: $method")
            }
        }
    }
}
