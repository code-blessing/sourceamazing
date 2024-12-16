package org.codeblessing.sourceamazing.builder.proxy

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.update.BuilderMethodInterpreterDataCollector
import org.codeblessing.sourceamazing.builder.update.BuilderUpdater
import org.codeblessing.sourceamazing.builder.validation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.builder.validation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromInjectBuilderParameter
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromReturnType
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.proxy.KotlinInvocationHandler
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.type.hasAnnotation
import org.codeblessing.sourceamazing.schema.type.valueParamsWithValues
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class BuilderInvocationHandler(
    private val schemaAccess: SchemaAccess,
    builderClass: KClass<*>,
    private val conceptDataCollector: ConceptDataCollector,
    superiorConcepts: Map<Alias, ConceptName>,
    private val superiorConceptIds: Map<Alias, ConceptIdentifier>
): KotlinInvocationHandler()  {

    private val builderClassInterpreter = BuilderClassInterpreter(
        builderClass = builderClass,
        newConceptNamesWithAliasFromSuperiorBuilder = superiorConcepts,
    )

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        val args = function.valueParamsWithValues(arguments)
        if(function.hasAnnotation(BuilderMethod::class)){

            val builderMethodInterpreter = BuilderMethodInterpreter(
                schemaAccess = schemaAccess,
                builderClassInterpreter = builderClassInterpreter,
                method = function,
            )

            val builderMethodInterpreterDataCollector = BuilderMethodInterpreterDataCollector(
                conceptDataCollector = conceptDataCollector,
                functionArguments =  args,
                newConceptIdsFromSuperiorBuilder = superiorConceptIds,
            )

            BuilderUpdater.updateConceptDataCollector(builderMethodInterpreter, builderMethodInterpreterDataCollector)

            val expectedSuperiorAndMyConcepts = builderMethodInterpreter.newConceptNamesAndExpectedConceptNamesFromSuperiorBuilder()
            val expectedSuperiorAndMyConceptIds = builderMethodInterpreterDataCollector.newConceptIdsAndSuperiorConceptIds()


            val subBuilderClassFromInjectBuilderAnnotation = getBuilderClassFromInjectBuilderParameter(function)
            if(subBuilderClassFromInjectBuilderAnnotation != null) {
                val builderForInjection: Any = createNewBuilderProxy(subBuilderClassFromInjectBuilderAnnotation, conceptDataCollector, expectedSuperiorAndMyConcepts, expectedSuperiorAndMyConceptIds)
                injectBuilderToParamMethod(function, args, builderForInjection)
                return null // if a builder is injected, the method can not return a builder
            }

            val subBuilderClassFromReturnType = getBuilderClassFromReturnType(function)
            if (subBuilderClassFromReturnType != null) {
                // if the return type is the same class as the proxy , we could also return the proxy itself
                val builderForReturnValue: Any = createNewBuilderProxy(subBuilderClassFromReturnType, conceptDataCollector, expectedSuperiorAndMyConcepts, expectedSuperiorAndMyConceptIds)
                return builderForReturnValue
            }

            // neither an injected nor a returned builder
            return null
        }

        throw IllegalArgumentException("Method $function has not the supported annotations (${BuilderMethod::class.shortText()}.")
    }

    private fun createNewBuilderProxy(
        builderClass: KClass<*>,
        dataCollector: ConceptDataCollector,
        aliasToConceptMap: Map<Alias, ConceptName>,
        aliasToConceptIdMap: Map<Alias, ConceptIdentifier>
    ): Any {
        return ProxyCreator.createProxy(
            definitionClass = builderClass,
            invocationHandler = BuilderInvocationHandler(
                schemaAccess = schemaAccess,
                builderClass = builderClass,
                conceptDataCollector = dataCollector,
                superiorConcepts = aliasToConceptMap,
                superiorConceptIds = aliasToConceptIdMap
            ),
        )
    }

    private fun injectBuilderToParamMethod(
        method: KFunction<*>,
        args: Map<KParameter, Any?>,
        builder: Any
    ) {
        getBuilderInjectionParameterFunctionOrNull(method, args)?.let { conceptBuilderFunctionParameter ->
            conceptBuilderFunctionParameter(builder)
        }
    }

    private fun getBuilderInjectionParameterFunctionOrNull(method: KFunction<*>, args: Map<KParameter, Any?>): ((Any) -> Unit)? {
        args.forEach { (parameter, argument) ->
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
}
