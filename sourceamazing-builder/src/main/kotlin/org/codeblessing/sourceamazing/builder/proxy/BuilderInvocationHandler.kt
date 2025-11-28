package org.codeblessing.sourceamazing.builder.proxy

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.update.BuilderMethodInterpreterDataCollector
import org.codeblessing.sourceamazing.builder.update.BuilderUpdater
import org.codeblessing.sourceamazing.schema.api.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.utils.proxy.KotlinInvocationHandler
import org.codeblessing.sourceamazing.utils.proxy.ProxyCreator
import org.codeblessing.sourceamazing.utils.type.hasAnnotation
import org.codeblessing.sourceamazing.utils.type.valueParamsWithValues

class BuilderInvocationHandler(
    private val schemaAccess: SchemaAccess,
    builderClass: KClass<*>,
    private val conceptDataCollector: ConceptDataCollector,
    private val superiorAliases: Map<Alias, ConceptNameAndIdentifier>,
) : KotlinInvocationHandler(allowMemberProperties = false, allowMemberFunctions = true) {

    private val builderClassInterpreter =
        BuilderClassInterpreter(
            builderClass = builderClass,
            newConceptNamesWithAliasFromSuperiorBuilder = superiorAliases.mapValues { it.value.conceptName },
        )

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        val args = function.valueParamsWithValues(arguments)
        if (function.hasAnnotation(BuilderMethod::class)) {

            val builderMethodInterpreter =
                BuilderMethodInterpreter(
                    schemaAccess = schemaAccess,
                    builderClassInterpreter = builderClassInterpreter,
                    method = function,
                )

            val builderMethodInterpreterDataCollector =
                BuilderMethodInterpreterDataCollector(
                    conceptDataCollector = conceptDataCollector,
                    functionArguments = args,
                    newConceptIdsFromSuperiorBuilder = superiorAliases.mapValues { it.value.conceptIdentifier },
                )

            BuilderUpdater.updateConceptDataCollector(builderMethodInterpreter, builderMethodInterpreterDataCollector)

            val expectedSuperiorAndOwnConcepts =
                builderMethodInterpreter.newConceptNamesAndExpectedConceptNamesFromSuperiorBuilder()
            val expectedSuperiorAndOwnConceptIds =
                builderMethodInterpreterDataCollector.newConceptIdsAndSuperiorConceptIds()

            val expectedSuperiorAliases: Map<Alias, ConceptNameAndIdentifier> =
                mergeConceptNamesAndIds(expectedSuperiorAndOwnConcepts, expectedSuperiorAndOwnConceptIds)

            val subBuilderClassFromInjectBuilderAnnotation =
                builderMethodInterpreter.getBuilderClassFromInjectBuilderParameter()
            if (subBuilderClassFromInjectBuilderAnnotation != null) {
                val builderForInjection: Any =
                    createNewBuilderProxy(
                        subBuilderClassFromInjectBuilderAnnotation,
                        conceptDataCollector,
                        aliases = expectedSuperiorAliases,
                    )
                injectBuilderToParamMethod(function, args, builderForInjection)
                return null // if a builder is injected, the method can not return a builder
            }

            val subBuilderClassFromReturnType = builderMethodInterpreter.getBuilderClassFromReturnType()
            if (subBuilderClassFromReturnType != null) {
                // if the return type is the same class as the proxy , we could also return the
                // proxy itself
                val builderForReturnValue: Any =
                    createNewBuilderProxy(
                        subBuilderClassFromReturnType,
                        conceptDataCollector,
                        aliases = expectedSuperiorAliases,
                    )
                return builderForReturnValue
            }

            // neither an injected nor a returned builder
            return null
        }

        throw IllegalArgumentException(
            "Method $function has not the supported annotations (${BuilderMethod::class.shortText()}."
        )
    }

    private fun mergeConceptNamesAndIds(
        expectedSuperiorAndOwnConcepts: Map<Alias, ConceptName>,
        expectedSuperiorAndOwnConceptIds: Map<Alias, ConceptIdentifier>,
    ): Map<Alias, ConceptNameAndIdentifier> {
        return expectedSuperiorAndOwnConcepts.mapValues { (alias, conceptName) ->
            ConceptNameAndIdentifier(
                conceptName,
                expectedSuperiorAndOwnConceptIds[alias]
                    ?: throw IllegalArgumentException(
                        "No concept id found for alias ${alias.name} " + "and concept ${conceptName.simpleName()}."
                    ),
            )
        }
    }

    private fun createNewBuilderProxy(
        builderClass: KClass<*>,
        dataCollector: ConceptDataCollector,
        aliases: Map<Alias, ConceptNameAndIdentifier>,
    ): Any {
        return ProxyCreator.createProxy(
            interfaceForProxy = builderClass,
            invocationHandler =
                BuilderInvocationHandler(
                    schemaAccess = schemaAccess,
                    builderClass = builderClass,
                    conceptDataCollector = dataCollector,
                    superiorAliases = aliases,
                ),
        )
    }

    private fun injectBuilderToParamMethod(method: KFunction<*>, args: Map<KParameter, Any?>, builder: Any) {
        getBuilderInjectionParameterFunctionOrNull(method, args)?.let { conceptBuilderFunctionParameter ->
            conceptBuilderFunctionParameter(builder)
        }
    }

    private fun getBuilderInjectionParameterFunctionOrNull(
        method: KFunction<*>,
        args: Map<KParameter, Any?>,
    ): ((Any) -> Unit)? {
        args.forEach { (parameter, argument) ->
            if (parameter.hasAnnotation(InjectBuilder::class)) {
                val builderToInjectFunction =
                    argument
                        ?: throw IllegalStateException(
                            "Parameter with Annotation ${InjectBuilder::class} found but was null on method: $method"
                        )
                try {
                    @Suppress("UNCHECKED_CAST")
                    return@getBuilderInjectionParameterFunctionOrNull builderToInjectFunction as (Any) -> Unit
                } catch (ex: Exception) {
                    throw IllegalStateException(
                        "Could not cast builder parameter marked with '${InjectBuilder::class.java}' in method '$method'. " +
                            "This must be a function receiving exactly one argument (the builder) and returning nothing. But was ${builderToInjectFunction}.",
                        ex,
                    )
                }
            }
        }
        return null
    }
}
