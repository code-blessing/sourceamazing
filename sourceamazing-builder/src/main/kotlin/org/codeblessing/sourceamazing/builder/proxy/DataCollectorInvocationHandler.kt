package org.codeblessing.sourceamazing.builder.proxy

import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.FacetModificationRule
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.typemirror.NewConceptAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedBooleanFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedEnumFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedIntFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetFixedStringFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.SetRandomConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactory
import org.codeblessing.sourceamazing.schema.util.AnnotationUtil
import org.codeblessing.sourceamazing.schema.util.ConceptIdentifierUtil
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.KClass

class DataCollectorInvocationHandler(
    private val conceptDataCollector: ConceptDataCollector,
    private val superiorAliases: Map<String, ConceptIdentifier>
): InvocationHandler  {

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any?>?): Any {
        val proxy: Any = InvocationHandlerHelper.requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = InvocationHandlerHelper.validatedMethod(methodOrNull)
        val args: Array<out Any?> = InvocationHandlerHelper.validatedArguments(methodOrNull, argsOrNull)

        val methodMirror = MirrorFactory.convertToMethodMirror(method)

        if(methodMirror.hasAnnotation(BuilderMethod::class)){
            val myAliases = updateConceptDataCollector(methodMirror, args)

            val builderForNextStep: Any = if(methodMirror.hasAnnotation(WithNewBuilder::class)) {
                createNewBuilderProxy(method, conceptDataCollector, myAliases)
            } else {
                proxy // use same builder
            }
            injectBuilderToParamMethod(method, args, builderForNextStep)
            return builderForNextStep
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method)
    }

    private fun updateConceptDataCollector(functionMirror: FunctionMirrorInterface, args: Array<out Any?>): Map<String, ConceptIdentifier> {
        val newConceptAliasData: Map<String, Pair<ConceptName, ConceptIdentifier>> = collectNewAliases(functionMirror, args)

        newConceptAliasData.values.forEach { (conceptName, conceptIdentifier) ->
            conceptDataCollector.existingOrNewConceptData(conceptName, conceptIdentifier)
        }

        val newConceptAliases: Map<String, ConceptIdentifier> = newConceptAliasData.mapValues { it.value.second }

        functionMirror.annotations.filterIsInstance<SetFixedBooleanFacetValueAnnotationMirror>().forEach { defaultBooleanAnnotation ->
            updateConceptData(
                conceptAlias = defaultBooleanAnnotation.conceptToModifyAlias,
                facetClazz = defaultBooleanAnnotation.facetToModify.provideMirror(),
                value = defaultBooleanAnnotation.value,
                facetModificationRule = defaultBooleanAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        functionMirror.annotations.filterIsInstance<SetFixedEnumFacetValueAnnotationMirror>().forEach { defaultEnumAnnotation ->
            updateConceptData(
                conceptAlias = defaultEnumAnnotation.conceptToModifyAlias,
                facetClazz = defaultEnumAnnotation.facetToModify.provideMirror(),
                value = defaultEnumAnnotation.value,
                facetModificationRule = defaultEnumAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        functionMirror.annotations.filterIsInstance<SetFixedIntFacetValueAnnotationMirror>().forEach { defaultIntAnnotation ->
            updateConceptData(
                conceptAlias = defaultIntAnnotation.conceptToModifyAlias,
                facetClazz = defaultIntAnnotation.facetToModify.provideMirror(),
                value = defaultIntAnnotation.value,
                facetModificationRule = defaultIntAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        functionMirror.annotations.filterIsInstance<SetFixedStringFacetValueAnnotationMirror>().forEach { defaultStringAnnotation ->
            updateConceptData(
                conceptAlias = defaultStringAnnotation.conceptToModifyAlias,
                facetClazz = defaultStringAnnotation.facetToModify.provideMirror(),
                value = defaultStringAnnotation.value,
                facetModificationRule = defaultStringAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }
        functionMirror.annotations.filterIsInstance<SetAliasConceptIdentifierReferenceFacetValueAnnotationMirror>().forEach { referenceValueAnnotation ->
            val referenceConceptId = conceptIdByAlias(referenceValueAnnotation.referencedConceptAlias, newConceptAliases)

            updateConceptData(
                conceptAlias = referenceValueAnnotation.conceptToModifyAlias,
                facetClazz = referenceValueAnnotation.facetToModify.provideMirror(),
                value = referenceConceptId,
                facetModificationRule = referenceValueAnnotation.facetModificationRule,
                newConceptAliases = newConceptAliases
            )
        }

        val paramsWithValues = functionMirror.withMethodArguments(args)
        paramsWithValues.forEach { (_, param, argumentValue) ->

            if(!param.hasAnnotation(SetFacetValue::class)) {
                return@forEach
            }
            if(argumentValue==null) {
                if(param.hasAnnotation(IgnoreNullFacetValue::class)) {
                    return@forEach // skip null values silently
                } else {
                    throw IllegalArgumentException("Can not pass null values for parameter '${param.name}' " +
                            "on method $functionMirror. If this is wanted, use the annotation '${IgnoreNullFacetValue::class.annotationText()}'")
                }
            }

            param.annotations.filterIsInstance<SetFacetValueAnnotationMirror>().forEach { facetValueAnnotation ->
                updateConceptData(
                    conceptAlias = facetValueAnnotation.conceptToModifyAlias,
                    facetClazz = facetValueAnnotation.facetToModify.provideMirror(),
                    value = argumentValue,
                    facetModificationRule = facetValueAnnotation.facetModificationRule,
                    newConceptAliases = newConceptAliases
                )
            }
        }

        return newConceptAliases
    }

    private fun conceptIdByAlias(conceptAlias: String, newConceptAliases: Map<String, ConceptIdentifier>): ConceptIdentifier {
        return newConceptAliases[conceptAlias] ?: superiorAliases[conceptAlias]
        ?: throw IllegalStateException("Can not find concept id for alias '$conceptAlias'.")
    }

    private fun collectNewAliases(functionMirror: FunctionMirrorInterface, args: Array<out Any?>): Map<String, Pair<ConceptName, ConceptIdentifier>> {
        val paramsWithArgumentValues = functionMirror.withMethodArguments(args)

        val newConceptsByAlias: MutableMap<String, ConceptName> = mutableMapOf()
        val newConceptsIdentifierByAlias: MutableMap<String, Pair<ConceptName, ConceptIdentifier>> = mutableMapOf()

        functionMirror.annotations.filterIsInstance<NewConceptAnnotationMirror>().forEach { newConceptAnnotation ->
            newConceptsByAlias[newConceptAnnotation.declareConceptAlias] = ConceptName.of(newConceptAnnotation.concept.provideMirror())
        }

        functionMirror.annotations.filterIsInstance<SetRandomConceptIdentifierValueAnnotationMirror>().forEach { autoRandomConceptIdentifierAnnotation ->
            val conceptAlias = autoRandomConceptIdentifierAnnotation.conceptToModifyAlias
            val conceptName = newConceptsByAlias[conceptAlias]
                ?: throw IllegalStateException("Can not find concept name for alias '$conceptAlias' on method $functionMirror")
            val conceptIdentifier = ConceptIdentifierUtil.random(conceptName)
            newConceptsIdentifierByAlias[conceptAlias] = Pair(conceptName, conceptIdentifier)

        }

        paramsWithArgumentValues.forEach { (_, methodParam, argumentValue) ->
            methodParam.annotations.filterIsInstance<SetConceptIdentifierValueAnnotationMirror>().forEach { conceptIdentifierValueAnnotation ->
                val conceptAlias = conceptIdentifierValueAnnotation.conceptToModifyAlias
                val conceptName = newConceptsByAlias[conceptAlias]
                    ?: throw IllegalStateException("Can not find concept name on parameter for alias '$conceptAlias' on method $functionMirror")
                if(argumentValue == null) {
                    throw IllegalArgumentException("Can not pass null value as concept identifier argument for parameter '${methodParam.name}' on method $functionMirror")
                }
                val conceptIdentifier = argumentValue as ConceptIdentifier
                newConceptsIdentifierByAlias[conceptAlias] = Pair(conceptName, conceptIdentifier)
            }
        }

        return newConceptsIdentifierByAlias
    }

    private fun updateConceptData(
        conceptAlias: String,
        facetClazz: ClassMirrorInterface,
        value: Any,
        facetModificationRule: FacetModificationRule,
        newConceptAliases: Map<String, ConceptIdentifier>
    ) {
        val conceptId: ConceptIdentifier = conceptIdByAlias(conceptAlias, newConceptAliases)
        val conceptData = conceptDataCollector.existingConceptData(conceptId)
        val facetName = FacetName.of(facetClazz)
        val facetValues = facetValues(value)
        when(facetModificationRule) {
            FacetModificationRule.ADD -> conceptData.addFacetValues(facetName, facetValues)
            FacetModificationRule.REPLACE -> conceptData.replaceFacetValues(facetName, facetValues)
        }
    }

    private fun facetValues(value: Any): List<Any> {
        return when(value) {
            is List<*> -> value.filterNotNull()
            is Set<*> -> value.filterNotNull().toList()
            is Array<*> -> value.filterNotNull().toList()
            else -> listOf(value)
        }
    }

    private fun createNewBuilderProxy(
        method: Method,
        dataCollector: ConceptDataCollector,
        aliasToConceptIdMap: Map<String, ConceptIdentifier>
    ): Any {
        val builderClass = getBuilderClazz(method)
        return ProxyCreator.createProxy(builderClass, DataCollectorInvocationHandler(dataCollector, aliasToConceptIdMap))
    }

    private fun injectBuilderToParamMethod(
        method: Method,
        args: Array<out Any?>,
        builder: Any
    ) {
        val conceptBuilderFunctionParameter = getBuilderParameter(method, args)

        if(conceptBuilderFunctionParameter != null) {
            val function: (Any) -> Unit = try {
                @Suppress("UNCHECKED_CAST")
                conceptBuilderFunctionParameter as (Any) -> Unit
            } catch (ex: Exception) {
                throw IllegalStateException("Could not cast builder parameter marked with '${InjectBuilder::class.java}' in method '$method'. " +
                        "This must be a function receiving exactly one argument (the builder) and returning nothing. But was ${conceptBuilderFunctionParameter}.", ex)
            }
            function(builder)
        }
    }

    private fun getBuilderClazz(method: Method): KClass<*> {
        if(AnnotationUtil.hasAnnotation(method, WithNewBuilder::class)) {
            return AnnotationUtil.getAnnotation(method, WithNewBuilder::class).builderClass
        }
        throw IllegalStateException("No Annotation ${WithNewBuilder::class} found on method: $method")
    }


    private fun getBuilderParameter(method: Method, args: Array<out Any?>): Any? {
        for ((index, parameter) in method.parameters.withIndex()) {
            if(AnnotationUtil.hasAnnotation(parameter, InjectBuilder::class)) {
                val builderToInject = args[index]
                return builderToInject
                    ?: throw IllegalStateException(
                        "Parameter with Annotation ${InjectBuilder::class} found but was null on method: $method",
                        )
            }
        }
        return null
    }
}
