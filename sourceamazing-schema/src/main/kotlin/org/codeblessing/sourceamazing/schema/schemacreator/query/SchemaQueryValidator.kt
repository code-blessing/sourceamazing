package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongConceptQueryMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptsAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeHelper
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProviderHelper.provideClassMirrors

object SchemaQueryValidator {
    @Throws(MalformedSchemaException::class)
    fun validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass: ClassMirrorInterface) {
        val possibleSchemaConceptClasses = schemaDefinitionClass.getAnnotationMirror(SchemaAnnotationMirror::class).concepts.provideClassMirrors().toSet()
        schemaDefinitionClass.methods.filter(TypeHelper::isNotFromKotlinAnyClass).forEach { method ->
            val queryConceptClasses = method.getAnnotationMirrorOrNull(QueryConceptsAnnotationMirror::class)?.concepts?.provideClassMirrors()
                ?: throw WrongConceptQueryMalformedSchemaException("The method is missing " +
                        "the annotation ${QueryConcepts::class.shortText()}. Method: $method")

            if(queryConceptClasses.isEmpty()) {
                throw WrongConceptQueryMalformedSchemaException("The method has an empty list " +
                        "for ${QueryConcepts::conceptClasses.name} on ${QueryConcepts::class.shortText()}. Method: $method")
            }

            if(method.valueParameters.isNotEmpty()) {
                throw WrongConceptQueryMalformedSchemaException("The method has arguments/parameters which is not allowed " +
                        "for methods annotated with ${QueryConcepts::class.shortText()}. Method: $method")
            }

            queryConceptClasses.forEach { queryConceptClass ->
                if(!possibleSchemaConceptClasses.contains(queryConceptClass)) {
                    throw WrongConceptQueryMalformedSchemaException("The method has a invalid " +
                            "concept class '${queryConceptClass.longText()}'. Valid concept classes " +
                            "are ${possibleSchemaConceptClasses.map { it.longText() }}. Method: $method")
                }
            }
        }
    }
}
