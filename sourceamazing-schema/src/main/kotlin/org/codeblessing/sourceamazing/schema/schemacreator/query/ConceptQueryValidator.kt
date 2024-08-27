package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongFacetQueryMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeHelper
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProviderHelper.provideClassMirrors

object ConceptQueryValidator {
    @Throws(MalformedSchemaException::class)
    fun validateAccessorMethodsOfConceptClass(conceptClass: ClassMirrorInterface) {

        val possibleFacetClasses = conceptClass.getAnnotationMirror(ConceptAnnotationMirror::class).facets.provideClassMirrors().toSet()
        conceptClass.methods.filter(TypeHelper::isNotFromKotlinAnyClass).forEach { method ->
            if(method.valueParameters.isNotEmpty()) {
                throw WrongFacetQueryMalformedSchemaException("The method has arguments/parameters " +
                        "which is not allowed for methods annotated with " +
                        "${QueryConcepts::class.shortText()} or ${QueryConceptIdentifierValue::class.shortText()}. Method: $method")
            }

            val queryFacetValueAnnotationMirror = method.getAnnotationMirrorOrNull(QueryFacetValueAnnotationMirror::class)
            if(queryFacetValueAnnotationMirror != null) {
                val queryFacetValueClass = queryFacetValueAnnotationMirror.facetClass.provideMirror()
                if(!possibleFacetClasses.contains(queryFacetValueClass)) {
                    throw WrongFacetQueryMalformedSchemaException("The method has a invalid " +
                            "facet class ${queryFacetValueClass.shortText()}. Valid facet classes " +
                            "are ${possibleFacetClasses.map { it.shortText() }}. Method: $method")
                }
            } else if(!method.hasAnnotation(QueryConceptIdentifierValue::class)) {
                throw WrongFacetQueryMalformedSchemaException("The method is missing " +
                        "one of the annotations ${QueryFacetValue::class.shortText()} " +
                        "or ${QueryConceptIdentifierValue::class.shortText()}. Method: $method")
            }
        }
    }
}
