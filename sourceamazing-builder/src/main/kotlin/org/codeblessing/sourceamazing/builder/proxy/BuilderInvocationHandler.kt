package org.codeblessing.sourceamazing.builder.proxy

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.allAliasesFromExpectedAliasFromSuperiorBuilderAnnotations
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.collectNewConceptAliases
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromInjectBuilderParameter
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromReturnType
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.proxy.KotlinInvocationHandler
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.hasAnnotation
import org.codeblessing.sourceamazing.schema.type.valueParamsWithValues
import org.codeblessing.sourceamazing.schema.util.ConceptIdentifierUtil
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotations

class BuilderInvocationHandler(
    builderClass: KClass<*>,
    private val conceptDataCollector: ConceptDataCollector,
    superiorAliases: Map<Alias, ConceptIdentifier>
): KotlinInvocationHandler()  {

    private val expectedAliasesFromSuperiorBuilder: Set<Alias> = allAliasesFromExpectedAliasFromSuperiorBuilderAnnotations(builderClass).toSet()

    private val expectedSuperiorAliases = superiorAliases
        .filterKeys { key -> key in expectedAliasesFromSuperiorBuilder }

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        if(function.hasAnnotation(BuilderMethod::class)){
            val myAliases = updateConceptDataCollector(function, arguments)

            val expectedSuperiorAndMyAliases = mergeMaps(expectedSuperiorAliases, myAliases)

            val subBuilderClassFromInjectBuilderAnnotation = getBuilderClassFromInjectBuilderParameter(function)
            if(subBuilderClassFromInjectBuilderAnnotation != null) {
                val builderForInjection: Any = createNewBuilderProxy(subBuilderClassFromInjectBuilderAnnotation, conceptDataCollector, expectedSuperiorAndMyAliases)
                injectBuilderToParamMethod(function, arguments, builderForInjection)
                return null // if a builder is injected, the method can not return a builder
            }

            val subBuilderClassFromReturnType = getBuilderClassFromReturnType(function)
            if (subBuilderClassFromReturnType != null) {
                // if the return type is the same class as the proxy , we could also return the proxy itself
                val builderForReturnValue: Any = createNewBuilderProxy(subBuilderClassFromReturnType, conceptDataCollector, expectedSuperiorAndMyAliases)
                return builderForReturnValue
            }

            // neither an injected nor a returned builder
            return null
        }

        throw IllegalArgumentException("Method $function has not the supported annotations (${BuilderMethod::class.shortText()}.")
    }

    private fun updateConceptDataCollector(method: KFunction<*>, args: List<Any?>): Map<Alias, ConceptIdentifier> {
        val conceptNameByAlias: Map<Alias, ConceptName> = collectNewConceptAliases(method)
        val newConceptAliasData: Map<Alias, Pair<ConceptName, ConceptIdentifier>> = collectNewAliasesConceptIdentifiers(method, conceptNameByAlias, args)

        newConceptAliasData.values.forEach { (conceptName, conceptIdentifier) ->
            conceptDataCollector.newConceptData(conceptName, conceptIdentifier)
        }

        val newConceptAliases: Map<Alias, ConceptIdentifier> = newConceptAliasData.mapValues { it.value.second }

        method.annotations.filterIsInstance<SetFixedBooleanFacetValue>().forEach { defaultBooleanAnnotation ->
            updateConceptData(
                conceptAlias = defaultBooleanAnnotation.conceptToModifyAlias.toAlias(),
                facetClazz = defaultBooleanAnnotation.facetToModify,
                value = defaultBooleanAnnotation.value,
                facetModificationRule = defaultBooleanAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        method.annotations.filterIsInstance<SetFixedEnumFacetValue>().forEach { defaultEnumAnnotation ->
            updateConceptData(
                conceptAlias = defaultEnumAnnotation.conceptToModifyAlias.toAlias(),
                facetClazz = defaultEnumAnnotation.facetToModify,
                value = defaultEnumAnnotation.value,
                facetModificationRule = defaultEnumAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        method.annotations.filterIsInstance<SetFixedIntFacetValue>().forEach { defaultIntAnnotation ->
            updateConceptData(
                conceptAlias = defaultIntAnnotation.conceptToModifyAlias.toAlias(),
                facetClazz = defaultIntAnnotation.facetToModify,
                value = defaultIntAnnotation.value,
                facetModificationRule = defaultIntAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        method.annotations.filterIsInstance<SetFixedStringFacetValue>().forEach { defaultStringAnnotation ->
            updateConceptData(
                conceptAlias = defaultStringAnnotation.conceptToModifyAlias.toAlias(),
                facetClazz = defaultStringAnnotation.facetToModify,
                value = defaultStringAnnotation.value,
                facetModificationRule = defaultStringAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        method.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValue>().forEach { referenceValueAnnotation ->
            val referenceConceptId = conceptIdByAlias(referenceValueAnnotation.referencedConceptAlias.toAlias(), newConceptAliases)

            updateConceptData(
                conceptAlias = referenceValueAnnotation.conceptToModifyAlias.toAlias(),
                facetClazz = referenceValueAnnotation.facetToModify,
                value = referenceConceptId,
                facetModificationRule = referenceValueAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }

        method.valueParamsWithValues(args).forEach { (param, argumentValue) ->

            if(!param.hasAnnotation(SetFacetValue::class)) {
                return@forEach
            }
            if(argumentValue==null) {
                if(param.hasAnnotation(IgnoreNullFacetValue::class)) {
                    return@forEach // skip null values silently
                } else {
                    throw IllegalArgumentException("Can not pass null values for parameter '${param.name}' " +
                            "on method $method. If this is wanted, use the annotation '${IgnoreNullFacetValue::class.annotationText()}'")
                }
            }

            param.findAnnotations<SetFacetValue>().forEach { facetValueAnnotation ->
                updateConceptData(
                    conceptAlias = facetValueAnnotation.conceptToModifyAlias.toAlias(),
                    facetClazz = facetValueAnnotation.facetToModify,
                    value = argumentValue,
                    facetModificationRule = facetValueAnnotation.facetModificationRule,
                    newConceptAliases = newConceptAliases
                )
            }
        }

        return newConceptAliases
    }

    private fun conceptIdByAlias(conceptAlias: Alias, newConceptAliases: Map<Alias, ConceptIdentifier>): ConceptIdentifier {
        return newConceptAliases[conceptAlias] ?: expectedSuperiorAliases[conceptAlias]
        ?: throw IllegalStateException("Can not find concept id for alias '$conceptAlias'.")
    }

    private fun collectNewAliasesConceptIdentifiers(
        method: KFunction<*>,
        newConceptsByAlias: Map<Alias, ConceptName>,
        args: List<Any?>
    ): Map<Alias, Pair<ConceptName, ConceptIdentifier>> {
        val newConceptsIdentifierByAlias: MutableMap<Alias, Pair<ConceptName, ConceptIdentifier>> = mutableMapOf()

        method.annotations.filterIsInstance<SetRandomConceptIdentifierValue>().forEach { setRandomConceptIdentifierValueAnnotation ->
            val conceptAlias = setRandomConceptIdentifierValueAnnotation.conceptToModifyAlias.toAlias()
            val conceptName = newConceptsByAlias[conceptAlias]
                ?: throw IllegalStateException("Can not find concept name for alias '$conceptAlias' on method $method")
            val conceptIdentifier = ConceptIdentifierUtil.random(conceptName)
            newConceptsIdentifierByAlias[conceptAlias] = Pair(conceptName, conceptIdentifier)
        }

        method.valueParamsWithValues(args).forEach { (methodParam, argumentValue) ->
            methodParam.annotations.filterIsInstance<SetConceptIdentifierValue>().forEach { conceptIdentifierValueAnnotation ->
                val conceptAlias = conceptIdentifierValueAnnotation.conceptToModifyAlias.toAlias()
                val conceptName = newConceptsByAlias[conceptAlias]
                    ?: throw IllegalStateException("Can not find concept name on parameter for alias '$conceptAlias' on method $method")
                if(argumentValue == null) {
                    throw IllegalArgumentException("Can not pass null value as concept identifier argument for parameter '${methodParam.name}' on method $method")
                }
                val conceptIdentifier = when(argumentValue) {
                    is ConceptIdentifier -> argumentValue
                    is String -> ConceptIdentifier.of(argumentValue)
                    else -> throw IllegalArgumentException("Concept identifier must be a ${String::class} or a ${ConceptIdentifier::class}.")
                }
                newConceptsIdentifierByAlias[conceptAlias] = Pair(conceptName, conceptIdentifier)
            }
        }

        return newConceptsIdentifierByAlias
    }

    private fun updateConceptData(
        conceptAlias: Alias,
        facetClazz: KClass<*>,
        value: Any,
        facetModificationRule: FacetModificationRule,
        newConceptAliases: Map<Alias, ConceptIdentifier>
    ) {
        val conceptId: ConceptIdentifier = conceptIdByAlias(conceptAlias, newConceptAliases)
        val conceptData = conceptDataCollector.existingConceptData(conceptId)
        val facetName = facetClazz.toFacetName()
        val facetValues = facetValues(value)
        when(facetModificationRule) {
            FacetModificationRule.ADD -> conceptData.addFacetValues(facetName, facetValues)
            FacetModificationRule.REPLACE -> conceptData.replaceFacetValues(facetName, facetValues)
        }
    }

    private fun facetValues(value: Any): List<Any> {
        // keep in sync with [org.codeblessing.sourceamazing.builder.validation.BuilderValidator.SUPPORTED_COLLECTION_TYPES]

        // having null values here should throw an exception if not @IgnoreNullValues
        // but as we don't allow nullable inner values in collection, this is never the case
        return when(value) {
            is List<*> -> value.filterNotNull()
            is Set<*> -> value.filterNotNull().toList()
            is Array<*> -> value.filterNotNull().toList()
            else -> listOf(value)
        }
    }

    private fun createNewBuilderProxy(
        builderClass: KClass<*>,
        dataCollector: ConceptDataCollector,
        aliasToConceptIdMap: Map<Alias, ConceptIdentifier>
    ): Any {
        return ProxyCreator.createProxy(builderClass, BuilderInvocationHandler(builderClass,dataCollector, aliasToConceptIdMap))
    }

    private fun injectBuilderToParamMethod(
        method: KFunction<*>,
        args: List<Any?>,
        builder: Any
    ) {
        getBuilderInjectionParameterFunctionOrNull(method, args)?.let { conceptBuilderFunctionParameter ->
            conceptBuilderFunctionParameter(builder)
        }
    }

    private fun getBuilderInjectionParameterFunctionOrNull(method: KFunction<*>, args: List<Any?>): ((Any) -> Unit)? {
        method.valueParamsWithValues(args).forEach { (parameter, argument) ->
            if(parameter.hasAnnotation(InjectBuilder::class)) {
                val builderToInjectFunction = argument ?: throw IllegalStateException(
                    "Parameter with Annotation ${InjectBuilder::class} found but was null on method: $method",
                )
                try {
                    @Suppress("UNCHECKED_CAST")
                    return@getBuilderInjectionParameterFunctionOrNull builderToInjectFunction as (Any) -> Unit
                } catch (ex: Exception) {
                    throw IllegalStateException("Could not cast builder parameter marked with '${InjectBuilder::class.java}' in method '$method'. " +
                            "This must be a function receiving exactly one argument (the builder) and returning nothing. But was ${builderToInjectFunction}.", ex)
                }
            }
        }
        return null
    }

    private fun mergeMaps(firstMap: Map<Alias, ConceptIdentifier>, secondDominantMap: Map<Alias, ConceptIdentifier>): Map<Alias, ConceptIdentifier> {
        return firstMap.toMutableMap().also { it.putAll(secondDominantMap)}
    }
}
