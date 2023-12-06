package org.codeblessing.sourceamazing.engine.process.schema.query

import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.engine.process.schema.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.engine.process.util.AnnotationUtil
import kotlin.reflect.KClass

object SchemaQueryValidator {
    @Throws(MalformedSchemaException::class)
    fun validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass: KClass<*>) {

        val possibleSchemaConceptClasses = AnnotationUtil.getAnnotation(schemaDefinitionClass, Schema::class).concepts.toSet()
        schemaDefinitionClass.java.methods.forEach { method ->
            if(!AnnotationUtil.hasAnnotation(method, QueryConcepts::class)) {
                throw MalformedSchemaException("The following method is missing " +
                        "the annotation ${QueryConcepts::class}: $method")
            }
            val queryConceptClasses = AnnotationUtil.getAnnotation(method, QueryConcepts::class).conceptClasses

            if(queryConceptClasses.isEmpty()) {
                throw MalformedSchemaException("The following method has an empty list " +
                        "for ${QueryConcepts::conceptClasses.name} on ${QueryConcepts::class}: $method")
            }

            if(method.parameterCount > 0) {
                throw MalformedSchemaException("The following method has arguments/parameters which is not allowed " +
                        "for methods annotated with ${QueryConcepts::class}: $method")
            }

            queryConceptClasses.forEach { queryConceptClass ->
                if(!possibleSchemaConceptClasses.contains(queryConceptClass)) {
                    throw MalformedSchemaException("The following method has a invalid " +
                            "concept class $queryConceptClass. Valid concept classes " +
                            "are $possibleSchemaConceptClasses. Method: $method")
                }
            }
        }
    }
}
