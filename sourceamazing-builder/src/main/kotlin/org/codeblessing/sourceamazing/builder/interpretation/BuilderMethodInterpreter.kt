package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.interpretation.CommonMethodInterpretationHelper.castConceptIdentifier
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ConceptIdentifierAnnotationData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationBaseData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.type.receiverParameter
import org.codeblessing.sourceamazing.schema.type.returnTypeOrNull
import org.codeblessing.sourceamazing.schema.type.valueParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters

class BuilderMethodInterpreter(
    val schemaAccess: SchemaAccess,
    val builderClassInterpreter: BuilderClassInterpreter,
    val method: KFunction<*>,
): BuilderMethodApi(), BuilderInterpreter {
    val methodLocation = MethodLocation.create(method)
    override fun allBuilderInterpreters(): List<BuilderInterpreter> {
        return listOf(this) + collectAllBuilderDataProviderInterpreter()
    }

    override fun getBuilderInterpreterNewConceptsIncludingDuplicates(): List<Pair<Alias, ConceptName>> {
        return CommonMethodInterpretationHelper.extractNewConceptsAsPair(listOf(method))
    }

    override fun getBuilderInterpreterAliasesToSetRandomConceptIdentifierValueIncludingDuplicates(): List<Alias> {
        return CommonMethodInterpretationHelper.extractAliasesToSetRandomConceptIdentifierValueIncludingDuplicates(listOf(method))
    }

    override fun getBuilderInterpreterAliasesToSetConceptIdentifierValueAliasesIncludingDuplicates(): List<Alias> {
        return method.valueParameters()
            .flatMap { parameter -> parameter.annotations.filterIsInstance<SetConceptIdentifierValue>() }
            .map { it.conceptToModifyAlias.toAlias()}
    }

    override fun getBuilderInterpreterManualAssignedConceptIdentifierAnnotationContent(dataContext: DataContext?): List<ConceptIdentifierAnnotationData> {
        return getBuilderMethodManualAssignedConceptIdentifierAnnotationContent(dataContext)
    }

    override fun getBuilderInterpreterFacetValueAnnotationContent(dataContext: DataContext?): List<FacetValueAnnotationContent> {
        return getFixedFacetValues(dataContext) + getMethodParamAssignedFacetValues(dataContext)
    }

    fun newConceptNamesAndExpectedConceptNamesFromSuperiorBuilder(): Map<Alias, ConceptName> {
        val newConceptsFromMethod: Map<Alias, ConceptName> = newConcepts()
        val expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName> =
            builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()
        return newConceptsFromMethod + expectedConceptsFromSuperiorMethod
    }

    private fun getFixedFacetValues(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        return CommonMethodInterpretationHelper.extractFixedFacetValues(method, methodLocation, schemaAccess, dataContext)
    }

    private fun getMethodParamAssignedFacetValues(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        return method.valueParameters.flatMap { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetFacetValue>().map { annotation ->
                val value = dataContext?.valueForMethodParameter(methodParameter)
                FacetValueAnnotationContent(
                    base = FacetValueAnnotationBaseData(
                        methodLocation = builderMethodParameterLocation(methodParameter),
                        alias = annotation.conceptToModifyAlias.toAlias(),
                        facetName = annotation.facetToModify.toFacetName(),
                        facetModificationRule = annotation.facetModificationRule,
                        annotation = annotation,
                        ignoreNullValue = isIgnoreNullValue(methodParameter),
                        type = builderMethodParameterType(methodParameter),
                        typeClass = null
                    ),
                    value = value
                )
            }
        }
    }

    private fun collectAllBuilderDataProviderInterpreter(): List<BuilderDataProviderInterpreter> {
        return method.valueParameters
            .filter { it.hasAnnotation<ProvideBuilderData>() }
            .map { methodParameter ->
                BuilderDataProviderInterpreter.createFromMethodParam(
                    methodParameter = methodParameter,
                    builderMethodInterpreter = this,
                    schemaAccess = schemaAccess
                )
            }
    }

    private fun getBuilderMethodManualAssignedConceptIdentifierAnnotationContent(dataContext: DataContext?): List<ConceptIdentifierAnnotationData> {
        return method.valueParameters.flatMap { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetConceptIdentifierValue>().map { annotation ->
                val conceptIdentifier = dataContext?.valueForMethodParameter(methodParameter)?.let { castConceptIdentifier(it) }
                ConceptIdentifierAnnotationData(
                    methodLocation = builderMethodParameterLocation(methodParameter),
                    alias = annotation.conceptToModifyAlias.toAlias(),
                    annotation = annotation,
                    ignoreNullValue = isIgnoreNullValue(methodParameter),
                    type = builderMethodParameterType(methodParameter),
                    conceptIdentifier = conceptIdentifier
                )
            }
        }
    }

    private fun builderMethodParameterLocation(methodParameter: KParameter): MethodLocation {
        return methodLocation.extendWithMethodParam(methodParameter)
    }

    private fun builderMethodParameterType(methodParameter: KParameter): KType {
        return methodParameter.type
    }

    private fun isIgnoreNullValue(methodParameter: KParameter): Boolean {
        return methodParameter.hasAnnotation<IgnoreNullFacetValue>()
    }


    fun getBuilderClassFromWithNewBuilderAnnotation(): KClass<*>? {
        return method.findAnnotation<WithNewBuilder>()?.builderClass
    }

    fun getBuilderClassFromInjectBuilderParameter(): KClass<*>? {
        val methodParameter = method.valueParameters.lastOrNull() ?: return null
        if(!methodParameter.hasAnnotation<InjectBuilder>()) {
            return null
        }

        val injectionBuilderKType = methodParameter.type
        val receiverParameterType = requireNotNull(injectionBuilderKType.receiverParameter()) {
            "receiverParameterType must not be null"
        }

        return KTypeUtil.classFromType(KTypeUtil.kTypeFromProjection(receiverParameterType))
    }

    fun getBuilderClassFromReturnType(): KClass<*>? {
        val methodReturnType = method.returnTypeOrNull() ?: return null
        return KTypeUtil.classesInformationFromKType(methodReturnType).first().clazz
    }
}