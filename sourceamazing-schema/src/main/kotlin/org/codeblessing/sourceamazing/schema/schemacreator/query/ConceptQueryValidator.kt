package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongFacetQueryMalformedSchemaException
import org.codeblessing.sourceamazing.schema.util.AnnotationUtil
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
