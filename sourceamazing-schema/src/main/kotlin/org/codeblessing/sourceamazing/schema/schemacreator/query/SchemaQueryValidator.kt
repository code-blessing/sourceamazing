package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongConceptQuerySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.type.FunctionCheckerUtil
import org.codeblessing.sourceamazing.schema.type.isFromKotlinAnyClass
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

object SchemaQueryValidator {
    private val SCHEMA_QUERY_FUNCTION_DESCRIPTION = "Function annotated with ${QueryConcepts::class.shortText()}"

    @Throws(SyntaxException::class)
    fun validateAccessorMethodsOfSchemaDefinitionClass(schemaDefinitionClass: KClass<*>) {
        val possibleSchemaConceptClasses = schemaDefinitionClass.annotations.filterIsInstance<Schema>().first().concepts.toSet()

        schemaDefinitionClass.memberFunctions.filterNot { it.isFromKotlinAnyClass() }.forEach { memberFunction ->
            FunctionCheckerUtil.checkHasNoValueParameters(memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)
            FunctionCheckerUtil.checkHasNoExtensionReceiverParameter(memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)
            FunctionCheckerUtil.checkHasNoTypeParameter(memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)
            FunctionCheckerUtil.checkHasNoFunctionBody(memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)

            val queryConceptClasses = memberFunction.findAnnotation<QueryConcepts>()?.conceptClasses
                ?: throw WrongConceptQuerySchemaSyntaxException("The method is missing " +
                        "the annotation ${QueryConcepts::class.shortText()}. Method: $memberFunction")

            if(queryConceptClasses.isEmpty()) {
                throw WrongConceptQuerySchemaSyntaxException("The method has an empty list " +
                        "for ${QueryConcepts::conceptClasses.name} on ${QueryConcepts::class.shortText()}. Method: $memberFunction")
            }


            queryConceptClasses.forEach { queryConceptClass ->
                if(!possibleSchemaConceptClasses.contains(queryConceptClass)) {
                    throw WrongConceptQuerySchemaSyntaxException("The method has a invalid " +
                            "concept class '${queryConceptClass.longText()}'. Valid concept classes " +
                            "are ${possibleSchemaConceptClasses.map { it.longText() }}. Method: $memberFunction")
                }
            }
        }
    }
}
