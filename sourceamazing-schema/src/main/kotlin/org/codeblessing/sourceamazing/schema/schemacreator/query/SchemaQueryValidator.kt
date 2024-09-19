package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongConceptQueryMalformedSchemaException
import org.codeblessing.sourceamazing.schema.type.isFromKotlinAnyClass
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

object SchemaQueryValidator {
    @Throws(MalformedSchemaException::class)
    fun validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass: KClass<*>) {
        val possibleSchemaConceptClasses = schemaDefinitionClass.annotations.filterIsInstance<Schema>().first().concepts.toSet()
        schemaDefinitionClass.memberFunctions.filterNot { it.isFromKotlinAnyClass() }.forEach { method ->
            val queryConceptClasses = method.findAnnotation<QueryConcepts>()?.conceptClasses
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
