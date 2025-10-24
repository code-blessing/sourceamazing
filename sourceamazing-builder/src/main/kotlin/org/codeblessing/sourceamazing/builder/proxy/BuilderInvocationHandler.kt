package org.codeblessing.sourceamazing.builder.proxy

import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.alias.TypeSafeBuilderContextImpl
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesHolder
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderRedeclarationHelper
import org.codeblessing.sourceamazing.builder.typesafeapi.TypeSafeBuilderContext
import org.codeblessing.sourceamazing.builder.update.BuilderMethodInterpreterDataCollector
import org.codeblessing.sourceamazing.builder.update.BuilderUpdater
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeClazzAndModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.TypeSafeSchemaContext
import org.codeblessing.sourceamazing.schema.utils.proxy.KotlinInvocationHandler
import org.codeblessing.sourceamazing.schema.utils.type.KFunctionUtil
import org.codeblessing.sourceamazing.schema.utils.type.hasAnnotation
import org.codeblessing.sourceamazing.schema.utils.type.valueParamsWithValues

class BuilderInvocationHandler(
    private val schemaContext: TypeSafeSchemaContext,
    builderClass: KClass<*>,
    private val builderFactoriesHolder: BuilderFactoriesHolder,
    private val superiorAliases: TypeSafeBuilderContext,
) : KotlinInvocationHandler(allowMemberProperties = false, allowMemberFunctions = true) {

    private val builderClassInterpreter =
        BuilderClassInterpreter(
            builderClass = builderClass,
            newClazzesWithAliasFromSuperiorBuilder = superiorAliases.onlyWithClazzes(),
        )

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        val tangibleFunction = KFunctionUtil.functionOrDerivedFunction(function, builderClassInterpreter.builderClass)
        val builderMethodInterpreter =
            BuilderMethodInterpreter(
                schemaAccess = schemaContext.schema,
                builderClassInterpreter = builderClassInterpreter,
                method = tangibleFunction,
            )
        return invokeInternal(proxy, builderMethodInterpreter, tangibleFunction.valueParamsWithValues(arguments))
    }

    private fun invokeInternal(
        proxy: Any,
        builderMethodInterpreter: BuilderMethodInterpreter,
        arguments: Map<KParameter, Any?>,
    ): Any? {
        if (builderMethodInterpreter.isBuilderMethod()) {
            return executeWithBuilderMethodAnnotation(builderMethodInterpreter, arguments)
        }

        if (builderMethodInterpreter.isDefaultMethod()) {
            return executeWithDefaultMethod(proxy, builderMethodInterpreter.method, arguments)
        }

        throw IllegalArgumentException(
            "Method ${builderMethodInterpreter.method} has not the supported annotations ${BuilderMethod::class.annotationText()} and is not a default method."
        )
    }

    private fun executeWithBuilderMethodAnnotation(
        builderMethodInterpreter: BuilderMethodInterpreter,
        arguments: Map<KParameter, Any?>,
    ): Any? {
        val builderMethodInterpreterDataCollector =
            BuilderMethodInterpreterDataCollector(
                clazzModelCollector = schemaContext.dataCollector,
                functionArguments = arguments,
                newClazzModelIdsFromSuperiorBuilder = superiorAliases.onlyWithClazzModelIds(),
            )

        BuilderUpdater.updateClazzModelCollector(builderMethodInterpreter, builderMethodInterpreterDataCollector)

        val expectedSuperiorAndOwnClazzes = builderMethodInterpreter.newClazzesAndExpectedClazzesFromSuperiorBuilder()
        val expectedSuperiorAndOwnClazzModelIds =
            builderMethodInterpreterDataCollector.newClazzModelIdsAndSuperiorClazzModelIds()

        val expectedSuperiorAliases: TypeSafeBuilderContext =
            mergeClazzesAndIds(
                expectedSuperiorAndOwnClazzes,
                expectedSuperiorAndOwnClazzModelIds,
                builderMethodInterpreter.aliasRedeclarations(),
            )

        val subBuilderClassFromInjectBuilderAnnotation =
            builderMethodInterpreter.getBuilderClassFromInjectBuilderParameter()
        if (subBuilderClassFromInjectBuilderAnnotation != null) {
            val builderForInjection: Any =
                BuilderProxyFactory.createNewBuilder(
                    schemaContext = schemaContext,
                    builderClass = subBuilderClassFromInjectBuilderAnnotation,
                    builderFactoriesHolder = builderFactoriesHolder,
                    builderContext = expectedSuperiorAliases,
                )
            injectBuilderToParamMethod(builderMethodInterpreter.method, arguments, builderForInjection)
        }

        val subBuilderClassFromReturnType = builderMethodInterpreter.getBuilderClassFromReturnType()
        if (subBuilderClassFromReturnType != null) {
            // if the return type is the same class as the proxy , we could also return the
            // proxy itself
            val builderForReturnValue: Any =
                BuilderProxyFactory.createNewBuilder(
                    schemaContext = schemaContext,
                    builderClass = subBuilderClassFromReturnType,
                    builderFactoriesHolder = builderFactoriesHolder,
                    builderContext = expectedSuperiorAliases,
                )
            return builderForReturnValue
        }

        // neither an injected nor a returned builder
        return null
    }

    private fun executeWithDefaultMethod(proxy: Any, function: KFunction<*>, arguments: Map<KParameter, Any?>): Any? {
        val javaMethod = requireNotNull(function.javaMethod) { "Method $function is not a default method" }
        val declaringClass = javaMethod.declaringClass
        val lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup())

        return lookup
            .unreflectSpecial(javaMethod, declaringClass)
            .bindTo(proxy)
            .invokeWithArguments(*(arguments.values.toTypedArray()))
    }

    private fun mergeClazzesAndIds(
        expectedSuperiorAndOwnClazzes: Map<Alias, Clazz>,
        expectedSuperiorAndOwnClazzModelIds: Map<Alias, ClazzModelId>,
        aliasRedeclarations: Map<Alias, Alias>,
    ): TypeSafeBuilderContext {
        val mergedClazzesAndIds =
            expectedSuperiorAndOwnClazzes.mapValues { (alias, clazz) ->
                TypeSafeClazzAndModelId(
                    clazz,
                    expectedSuperiorAndOwnClazzModelIds[alias]
                        ?: throw IllegalArgumentException(
                            "No clazz id found for alias ${alias.name} " + "and clazz ${clazz.simpleName()}."
                        ),
                )
            }
        return TypeSafeBuilderContextImpl(
            BuilderRedeclarationHelper.mapRedeclarations(mergedClazzesAndIds, aliasRedeclarations)
        )
    }

    private fun injectBuilderToParamMethod(method: KFunction<*>, args: Map<KParameter, Any?>, builder: Any) {
        getBuilderInjectionParameterFunctionOrNull(method, args)?.let { clazzBuilderFunctionParameter ->
            clazzBuilderFunctionParameter(builder)
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
