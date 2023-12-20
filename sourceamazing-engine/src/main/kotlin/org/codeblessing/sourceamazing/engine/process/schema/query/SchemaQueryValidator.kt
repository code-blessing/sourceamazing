package org.codeblessing.sourceamazing.engine.process.schema.query

import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.engine.process.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.WrongConceptQueryMalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.util.AnnotationUtil
import kotlin.reflect.KClass

object SchemaQueryValidator {
    @Throws(MalformedSchemaException::class)
    fun validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass: KClass<*>) {

        val possibleSchemaConceptClasses = AnnotationUtil.getAnnotation(schemaDefinitionClass, Schema::class).concepts.toSet()
        schemaDefinitionClass.java.methods.forEach { method ->
            if(!AnnotationUtil.hasAnnotation(method, QueryConcepts::class)) {
                throw WrongConceptQueryMalformedSchemaException("The method is missing " +
                        "the annotation ${QueryConcepts::class.shortText()}. Method: $method")
            }
            val queryConceptClasses = AnnotationUtil.getAnnotation(method, QueryConcepts::class).conceptClasses

            if(queryConceptClasses.isEmpty()) {
                throw WrongConceptQueryMalformedSchemaException("The method has an empty list " +
                        "for ${QueryConcepts::conceptClasses.name} on ${QueryConcepts::class.shortText()}. Method: $method")
            }

            if(method.parameterCount > 0) {
                throw WrongConceptQueryMalformedSchemaException("The method has arguments/parameters which is not allowed " +
                        "for methods annotated with ${QueryConcepts::class.shortText()}. Method: $method")
            }

            queryConceptClasses.forEach { queryConceptClass ->
                if(!possibleSchemaConceptClasses.contains(queryConceptClass)) {
                    throw WrongConceptQueryMalformedSchemaException("The method has a invalid " +
                            "concept class '${queryConceptClass.longText()}'. Valid concept classes " +
                            "are $possibleSchemaConceptClasses. Method: $method")
                }
            }
        }
    }
}
