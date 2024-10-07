package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongFacetQuerySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.type.isFromKotlinAnyClass
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

object ConceptQueryValidator {
    @Throws(SyntaxException::class)
    fun validateAccessorMethodsOfConceptClass(conceptClass: KClass<*>) {

        val possibleFacetClasses = conceptClass.findAnnotations<Concept>().first().facets.toSet()
        conceptClass.memberFunctions.filterNot { it.isFromKotlinAnyClass() }.forEach { method ->
            if(method.valueParameters.isNotEmpty()) {
                throw WrongFacetQuerySchemaSyntaxException("The method has arguments/parameters " +
                        "which is not allowed for methods annotated with " +
                        "${QueryConcepts::class.shortText()} or ${QueryConceptIdentifierValue::class.shortText()}. Method: $method")
            }

            val queryFacetValueAnnotationMirror = method.findAnnotation<QueryFacetValue>()
            if(queryFacetValueAnnotationMirror != null) {
                val queryFacetValueClass = queryFacetValueAnnotationMirror.facetClass
                if(!possibleFacetClasses.contains(queryFacetValueClass)) {
                    throw WrongFacetQuerySchemaSyntaxException("The method has a invalid " +
                            "facet class ${queryFacetValueClass.shortText()}. Valid facet classes " +
                            "are ${possibleFacetClasses.map { it.shortText() }}. Method: $method")
                }
            } else if(!method.hasAnnotation<QueryConceptIdentifierValue>()) {
                throw WrongFacetQuerySchemaSyntaxException("The method is missing " +
                        "one of the annotations ${QueryFacetValue::class.shortText()} " +
                        "or ${QueryConceptIdentifierValue::class.shortText()}. Method: $method")
            }
        }
    }
}
