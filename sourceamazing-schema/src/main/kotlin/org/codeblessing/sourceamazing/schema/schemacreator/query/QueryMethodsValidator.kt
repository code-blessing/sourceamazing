package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.RelevantMethodFetcher
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
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
import org.codeblessing.sourceamazing.schema.schemacreator.query.QueryMethodUtil.supportedCollectionClasses
import org.codeblessing.sourceamazing.schema.type.FunctionCheckerUtil
import org.codeblessing.sourceamazing.schema.type.KClassUtil
import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.schema.type.getAnnotation
import org.codeblessing.sourceamazing.schema.type.getAnnotationIncludingSuperclasses
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

object QueryMethodsValidator {
    private val SCHEMA_QUERY_FUNCTION_DESCRIPTION = "Function annotated with ${QueryConcepts::class.shortText()}"
    private val CONCEPT_QUERY_FUNCTION_DESCRIPTION = "Function annotated with ${QueryConcepts::class.shortText()} or ${QueryConceptIdentifierValue::class.shortText()}"

    @Throws(SyntaxException::class)
    fun validateQueryMethodsOfSchema(schemaDefinitionClass: KClass<*>) {
        val possibleSchemaConceptClasses = schemaDefinitionClass.annotations.filterIsInstance<Schema>().first().concepts.toSet()
        validateQueryMethods(schemaDefinitionClass, SCHEMA_QUERY_FUNCTION_DESCRIPTION)

        RelevantMethodFetcher.ownMemberFunctions(schemaDefinitionClass).forEach { memberFunction ->
            val queryConceptClasses = memberFunction.findAnnotation<QueryConcepts>()?.conceptClasses
                ?: throw WrongConceptQuerySchemaSyntaxException(memberFunction, SchemaErrorCode.MISSING_QUERY_CONCEPT_ANNOTATION)

            if(queryConceptClasses.isEmpty()) {
                throw WrongConceptQuerySchemaSyntaxException(memberFunction, SchemaErrorCode.NO_CONCEPTS_TO_QUERY)
            }


            queryConceptClasses.forEach { queryConceptClass ->
                if(!possibleSchemaConceptClasses.contains(queryConceptClass)) {
                    throw WrongConceptQuerySchemaSyntaxException(memberFunction, SchemaErrorCode.INVALID_CONCEPT_TO_QUERY, queryConceptClass.longText(), possibleSchemaConceptClasses.map { it.longText() })
                }
            }

            val returnTypeClassesInformation = classesInformationFromReturnType(memberFunction, SCHEMA_QUERY_FUNCTION_DESCRIPTION)
            val returnTypeValueClassInfo = QueryMethodUtil.valueClassInfo(returnTypeClassesInformation)

            if(!isInheritanceCompatibleClass(returnTypeValueClassInfo.clazz, queryConceptClasses)) {
                throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE, returnTypeValueClassInfo.clazz.longText(), queryConceptClasses.toList().map { it.longText() })
            }
        }
    }

    @Throws(SyntaxException::class)
    fun validateQueryMethodsOfConcept(conceptClass: KClass<*>) {
        val facetClassesOfThisConcept = conceptClass.findAnnotations<Concept>().first().facets.toSet()
        validateQueryMethods(conceptClass, CONCEPT_QUERY_FUNCTION_DESCRIPTION)

        RelevantMethodFetcher.ownMemberFunctions(conceptClass).forEach { memberFunction ->
            val returnTypeClassesInformation = classesInformationFromReturnType(memberFunction, CONCEPT_QUERY_FUNCTION_DESCRIPTION)
            val returnTypeCollectionClassInfo = QueryMethodUtil.collectionClassInfo(returnTypeClassesInformation)
            val returnTypeValueClassInfo = QueryMethodUtil.valueClassInfo(returnTypeClassesInformation)

            if(memberFunction.hasAnnotation<QueryFacetValue>()) {
                val queryFacetValue = memberFunction.getAnnotation<QueryFacetValue>()
                val queryFacetValueClass = queryFacetValue.facetClass
                if(!facetClassesOfThisConcept.contains(queryFacetValueClass)) {
                    throw WrongFacetQuerySchemaSyntaxException(memberFunction, SchemaErrorCode.INVALID_FACET_TO_QUERY, queryFacetValueClass.shortText(), facetClassesOfThisConcept.map { it.shortText() })
                }

                val supportedValueTypes = getValueTypeForFacet(queryFacetValue)
                if(!supportedValueTypes.contains(returnTypeValueClassInfo.clazz)) {
                    throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.FACET_RETURN_TYPE_NOT_SUPPORTED, queryFacetValueClass.longText(), returnTypeValueClassInfo.clazz.longText(), supportedValueTypes.toList().map { it.shortText() })
                }
            } else if(memberFunction.hasAnnotation<QueryConceptIdentifierValue>()) {
                if(returnTypeCollectionClassInfo != null) {
                    throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.RETURN_TYPE_COLLECTION_TO_FETCH_CONCEPT_IDENTIFIER_NOT_SUPPORTED)
                }

                val supportedValueTypes = listOf<KClass<*>>(Any::class, String::class, ConceptIdentifier::class)
                if(!supportedValueTypes.contains(returnTypeValueClassInfo.clazz)) {
                    throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.RETURN_TYPE_TO_FETCH_CONCEPT_IDENTIFIER_NOT_SUPPORTED, returnTypeValueClassInfo.clazz.longText(), supportedValueTypes.toList().map { it.shortText() })
                }
            } else {
                throw WrongFacetQuerySchemaSyntaxException(memberFunction, SchemaErrorCode.NO_FACET_TO_QUERY)
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
            return listOf(Any::class, String::class, queryFacetValueClass.getAnnotationIncludingSuperclasses<EnumFacet>().enumerationClass)
        } else if(queryFacetValueClass.hasAnnotation<ReferenceFacet>()) {
            val referencedConcepts = queryFacetValueClass.getAnnotationIncludingSuperclasses<ReferenceFacet>().referencedConcepts.toList()
            return KClassUtil.findAllCommonBaseClasses(referencedConcepts).toList()
        }
        throw IllegalStateException("Facet type not supported.")
    }

    private fun validateQueryMethods(definitionClass: KClass<*>, classDescription: String) {
        RelevantMethodFetcher.ownMemberFunctions(definitionClass).forEach { memberFunction ->
            FunctionCheckerUtil.checkHasNoValueParameters(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasNoExtensionReceiverParameter(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasNoTypeParameter(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasNoFunctionBody(memberFunction, classDescription)
            FunctionCheckerUtil.checkHasReturnType(memberFunction, classDescription)

            val returnTypeClassesInformation = classesInformationFromReturnType(memberFunction, classDescription)
            val returnTypeCollectionClassInfo = QueryMethodUtil.collectionClassInfo(returnTypeClassesInformation)
            val returnTypeValueClassInfo = QueryMethodUtil.valueClassInfo(returnTypeClassesInformation)

            if(returnTypeCollectionClassInfo != null) {
                if(supportedCollectionClasses.none { returnTypeCollectionClassInfo.clazz == it  }) {
                    throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS, supportedCollectionClasses, returnTypeCollectionClassInfo.clazz.longText())
                }

                if(returnTypeValueClassInfo.isValueNullable) {
                    throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED)
                }
            }
        }
    }

    private fun classesInformationFromReturnType(memberFunction: KFunction<*>, definitionClass: String): List<KTypeClassInformation> {
        val classesInformation = try {
            KTypeUtil.classesInformationFromKType(memberFunction.returnType)
        } catch (ex: IllegalStateException) {
            throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.RETURN_TYPE_IS_INVALID, definitionClass, ex.message ?: "")
        }

        if (classesInformation.size > 2 || classesInformation.isEmpty()) {
            throw WrongFunctionSyntaxException(memberFunction, SchemaErrorCode.RETURN_TYPE_IS_INVALID_ONLY_COLLECTION_OR_CLASS, definitionClass, supportedCollectionClasses)
        }
        return classesInformation
    }

    private fun isInheritanceCompatibleClass(clazz: KClass<*>, classesCompatibleWithClazz: Array<KClass<*>>): Boolean {
        return classesCompatibleWithClazz.all { classCompatibleWithClazz ->
            classCompatibleWithClazz == clazz || classCompatibleWithClazz.isSubclassOf(clazz)
        }
    }
}
