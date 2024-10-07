package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongConceptQuerySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongFacetQuerySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.type.FunctionCheckerUtil
import org.codeblessing.sourceamazing.schema.type.KClassUtil
import org.codeblessing.sourceamazing.schema.type.KTypeCheckerUtil.classFromProjection
import org.codeblessing.sourceamazing.schema.type.KTypeCheckerUtil.classFromType
import org.codeblessing.sourceamazing.schema.type.getAnnotation
import org.codeblessing.sourceamazing.schema.type.isFromKotlinAnyClass
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.superclasses

object QueryMethodsValidator {
    private val SCHEMA_QUERY_FUNCTION_DESCRIPTION = "Function annotated with ${QueryConcepts::class.shortText()}"
    private val CONCEPT_QUERY_FUNCTION_DESCRIPTION = "Function annotated with ${QueryConcepts::class.shortText()} or ${QueryConceptIdentifierValue::class.shortText()}"

    @Throws(SyntaxException::class)
    fun validateQueryMethodsOfSchema(schemaDefinitionClass: KClass<*>) {
        val possibleSchemaConceptClasses = schemaDefinitionClass.annotations.filterIsInstance<Schema>().first().concepts.toSet()
        validateQueryMethods(schemaDefinitionClass, SCHEMA_QUERY_FUNCTION_DESCRIPTION)

        relevantQueryMethods(schemaDefinitionClass).forEach { memberFunction ->
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

            val collectionAndValueInformation = collectionAndValueInformation(memberFunction)

            if(!isInheritanceCompatibleClass(collectionAndValueInformation.valueClass, queryConceptClasses)) {
                throw WrongFunctionSyntaxException("The return type class ${collectionAndValueInformation.valueClass} must be inheritable for all concepts ${queryConceptClasses.toList()}. Method: $memberFunction")
            }
        }
    }

    @Throws(SyntaxException::class)
    fun validateQueryMethodsOfConcept(conceptClass: KClass<*>) {
        val facetClassesOfThisConcept = conceptClass.findAnnotations<Concept>().first().facets.toSet()
        validateQueryMethods(conceptClass, CONCEPT_QUERY_FUNCTION_DESCRIPTION)

        relevantQueryMethods(conceptClass).forEach { memberFunction ->
            val collectionAndValueInformation = collectionAndValueInformation(memberFunction)
            if(memberFunction.hasAnnotation<QueryFacetValue>()) {
                val queryFacetValue = memberFunction.getAnnotation<QueryFacetValue>()
                val queryFacetValueClass = queryFacetValue.facetClass
                if(!facetClassesOfThisConcept.contains(queryFacetValueClass)) {
                    throw WrongFacetQuerySchemaSyntaxException("The method has a invalid " +
                            "facet class ${queryFacetValueClass.shortText()}. Valid facet classes " +
                            "are ${facetClassesOfThisConcept.map { it.shortText() }}. Method: $memberFunction")
                }

                val supportedValueTypes = getValueTypeForFacet(queryFacetValue)
                if(!supportedValueTypes.contains(collectionAndValueInformation.valueClass)) {
                    throw WrongFunctionSyntaxException(
                                "The method return type for the facet $queryFacetValueClass was ${collectionAndValueInformation.valueClass}," +
                                "which is not a supported value type. " +
                                "Valid return types are ${supportedValueTypes.map { it.shortText() }}. " +
                                "Method: $memberFunction"
                    )

                }
            } else if(memberFunction.hasAnnotation<QueryConceptIdentifierValue>()) {
                if(collectionAndValueInformation.collectionClass != null) {
                    throw WrongFunctionSyntaxException(
                        "The method return type for a method to fetch a concept identifier " +
                                "can not return a collection. Method: $memberFunction"
                    )
                }

                val supportedValueTypes = listOf<KClass<*>>(Any::class, String::class, ConceptIdentifier::class)
                if(!supportedValueTypes.contains(collectionAndValueInformation.valueClass)) {
                    throw WrongFunctionSyntaxException(
                                "The method return type for a method to fetch a concept identifier " +
                                "was ${collectionAndValueInformation.valueClass}, which is not a supported value type." +
                                "Valid return types are ${supportedValueTypes.map { it.shortText() }}. " +
                                "Method: $memberFunction"
                    )
                }
            } else {
                throw WrongFacetQuerySchemaSyntaxException("The method is missing " +
                        "one of the annotations ${QueryFacetValue::class.shortText()} " +
                        "or ${QueryConceptIdentifierValue::class.shortText()}. Method: $memberFunction")
            }
        }
    }

    private fun getValueTypeForFacet(queryFacetValue: QueryFacetValue): List<KClass<*>> {
        val queryFacetValueClass = queryFacetValue.facetClass
        if(queryFacetValueClass.hasAnnotation<StringFacet>()) {
            return listOf(Any::class, String::class)
        } else if (queryFacetValueClass.hasAnnotation<BooleanFacet>()) {
            return listOf(Any::class, Boolean::class)
        } else if (queryFacetValueClass.hasAnnotation<IntFacet>()) {
            return listOf(Any::class, Int::class)
        } else if (queryFacetValueClass.hasAnnotation<EnumFacet>()) {
            return listOf(Any::class, String::class, queryFacetValueClass.getAnnotation<EnumFacet>().enumerationClass)
        } else if(queryFacetValueClass.hasAnnotation<ReferenceFacet>()) {
            val referencedConcepts = queryFacetValueClass.getAnnotation<ReferenceFacet>().referencedConcepts.toList()
            return KClassUtil.findAllCommonBaseClasses(referencedConcepts).toList()
        }
        throw IllegalStateException("Facet type not supported.")
    }

    private fun relevantQueryMethods(definitionClass: KClass<*>): List<KFunction<*>> {
        return definitionClass.memberFunctions.filterNot { it.isFromKotlinAnyClass() }
    }

    private fun validateQueryMethods(definitionClass: KClass<*>, classDescription: String) {
        relevantQueryMethods(definitionClass).forEach { memberFunction ->
            FunctionCheckerUtil.checkHasNoValueParameters(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasNoExtensionReceiverParameter(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasNoTypeParameter(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasNoFunctionBody(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasReturnType(memberFunction, classDescription)
            FunctionCheckerUtil.checkReturnTypeIsClass(memberFunction, classDescription)

            val collectionAndValueInformation = collectionAndValueInformation(memberFunction)

            if(collectionAndValueInformation.collectionClass != null) {
                val supportedCollectionClasses: List<KClass<*>> = listOf(List::class, Set::class, Collection::class, Iterable::class)
                if(supportedCollectionClasses.none { collectionAndValueInformation.collectionClass == it  }) {
                    throw WrongFunctionSyntaxException("The collection type of the function must be one of ${supportedCollectionClasses}. Method: $memberFunction")
                }
            }

            if(collectionAndValueInformation.collectionClass != null && collectionAndValueInformation.isValueNullable) {
                throw WrongFunctionSyntaxException("Returning a collection with values marked as nullable (inner generic type) is not allowed. Method: $memberFunction")
            }
        }
    }

    private fun isInheritanceCompatibleClass(clazz: KClass<*>, classesCompatibleWithClazz: Array<KClass<*>>): Boolean {
        return classesCompatibleWithClazz.all { classCompatibleWithClazz ->
            classCompatibleWithClazz == clazz || classCompatibleWithClazz.isSubclassOf(clazz)
        }
    }

    private data class CollectionAndValueInformation(
        val collectionClass: KClass<*>?,
        val isCollectionNullable: Boolean,
        val valueClass: KClass<*>,
        val isValueNullable: Boolean,
    )

    private fun collectionAndValueInformation(memberFunction: KFunction<*>): CollectionAndValueInformation {
        val returnType = memberFunction.returnType
        val collectionClass: KClass<*>?
        val isCollectionNullable: Boolean
        val valueClass: KClass<*>
        val isValueNullable: Boolean
        if(returnType.arguments.isEmpty()) {
            collectionClass = null
            isCollectionNullable = false
            valueClass = classFromType(returnType, memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)
            isValueNullable = returnType.isMarkedNullable
        } else {
            collectionClass = classFromType(returnType, memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)
            isCollectionNullable = returnType.isMarkedNullable
            valueClass = classFromProjection(returnType.arguments[0], memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)
            isValueNullable = returnType.arguments[0].type?.isMarkedNullable == true
        }

        return CollectionAndValueInformation(
            collectionClass = collectionClass,
            isCollectionNullable = isCollectionNullable,
            valueClass = valueClass,
            isValueNullable = isValueNullable,
        )
    }


}
