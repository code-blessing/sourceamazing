package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreProvidedNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.builder.exceptions.DataProviderInvocationRuntimeException
import org.codeblessing.sourceamazing.builder.interpretation.CommonMethodInterpretationHelper.castConceptIdentifier
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ConceptIdentifierAnnotationData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationBaseData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.RelevantMethodFetcher
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.hasAnnotation

class BuilderDataProviderInterpreter(
    private val schemaAccess: SchemaAccess,
    private val dataProviderLocation: MethodLocation,
    private val dataProviderInstanceSupplier: BuilderDataProviderInstanceSupplier,
    val dataProviderClass: KClass<*>
): BuilderInterpreter {

    companion object {
        fun createFromMethodParam(
            methodParameter: KParameter,
            builderMethodInterpreter: BuilderMethodInterpreter,
            schemaAccess: SchemaAccess
        ): BuilderDataProviderInterpreter {
            val builderMethodLocation = builderMethodInterpreter.methodLocation.extendWithMethodParam(methodParameter)
            val dataProviderClass = KTypeUtil.classFromType(methodParameter.type)

            val dataProviderInstanceSupplier = BuilderDataProviderInstanceSupplier { dataContext ->
                    requireNotNull(dataContext.valueForMethodParameter(methodParameter)) {
                        "The instance for the data provider class $dataProviderClass could not be found."
                    }
                }

            return BuilderDataProviderInterpreter(
                schemaAccess = schemaAccess,
                dataProviderLocation = builderMethodLocation.extendWithClass(dataProviderClass),
                dataProviderClass = dataProviderClass,
                dataProviderInstanceSupplier = dataProviderInstanceSupplier
            )
        }
    }

    override fun getBuilderInterpreterNewConceptsIncludingDuplicates(): List<Pair<Alias, ConceptName>> {
        return CommonMethodInterpretationHelper.extractNewConceptsAsPair(getBuilderDataMethods())
    }

    override fun getBuilderInterpreterAliasesToSetRandomConceptIdentifierValueIncludingDuplicates(): List<Alias> {
        return CommonMethodInterpretationHelper.extractAliasesToSetRandomConceptIdentifierValueIncludingDuplicates(
            getBuilderDataMethods()
        )
    }

    override fun getBuilderInterpreterAliasesToSetConceptIdentifierValueAliasesIncludingDuplicates(): List<Alias> {
        return getBuilderDataMethods()
            .flatMap { parameter -> parameter.annotations.filterIsInstance<SetProvidedConceptIdentifierValue>() }
            .map { it.conceptToModifyAlias.toAlias()}
    }

    override fun getBuilderInterpreterFacetValueAnnotationContent(dataContext: DataContext?): List<FacetValueAnnotationContent> {
        return getFixedFacetValuesOfBuilderDataMethods(dataContext) + getFacetValuesOfBuilderDataMethods(dataContext)
    }

    override fun getBuilderInterpreterManualAssignedConceptIdentifierAnnotationContent(dataContext: DataContext?): List<ConceptIdentifierAnnotationData> {
        return getBuilderDataMethods().flatMap { builderDataMethod ->
            builderDataMethod.annotations.filterIsInstance<SetProvidedConceptIdentifierValue>().map { annotation ->
                val conceptIdentifier = getBuilderDataValue(builderDataMethod, dataContext)?.let { castConceptIdentifier(it) }
                ConceptIdentifierAnnotationData(
                    methodLocation = builderMethodLocation(builderDataMethod),
                    alias = annotation.conceptToModifyAlias.toAlias(),
                    annotation = annotation,
                    ignoreNullValue = isIgnoreNullValue(builderDataMethod),
                    type = builderDataMethodDataType(builderDataMethod),
                    conceptIdentifier = conceptIdentifier
                )
            }
        }
    }

    private fun getFixedFacetValuesOfBuilderDataMethods(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        return getBuilderDataMethods().flatMap { builderDataMethod ->
            CommonMethodInterpretationHelper.extractFixedFacetValues(
                builderDataMethod,
                builderMethodLocation(builderDataMethod),
                schemaAccess,
                dataContext
            )
        }
    }

    private fun getBuilderDataValue(method: KFunction<*>, dataContext: DataContext? = null): Any? {
        if(dataContext == null) return null

        val dataProviderInstance = dataProviderInstanceSupplier.getBuilderDataProviderInstance(dataContext)
        val exceptionMessage = "Exception during call of $method"
        try {
            return method.call(dataProviderInstance)
        } catch (ex: InvocationTargetException) {
            throw DataProviderInvocationRuntimeException(exceptionMessage, ex.targetException)
        } catch (ex: Exception) {
            throw DataProviderInvocationRuntimeException(exceptionMessage, ex)
        }
    }

    private fun getFacetValuesOfBuilderDataMethods(dataContext: DataContext? = null): List<FacetValueAnnotationContent> {
        return getBuilderDataMethods().flatMap { builderDataMethod ->

            builderDataMethod.annotations.filterIsInstance<SetProvidedFacetValue>().map { annotation ->
                val value = getBuilderDataValue(builderDataMethod, dataContext)
                FacetValueAnnotationContent(
                    base = FacetValueAnnotationBaseData(
                        methodLocation = builderMethodLocation(builderDataMethod),
                        alias = annotation.conceptToModifyAlias.toAlias(),
                        facetName = annotation.facetToModify.toFacetName(),
                        facetModificationRule = annotation.facetModificationRule,
                        annotation = annotation,
                        ignoreNullValue = isIgnoreNullValue(builderDataMethod),
                        type = builderDataMethodDataType(builderDataMethod),
                        typeClass = null
                    ),
                    value = value
                )
            }
        }
    }

    fun builderMethodLocation(builderDataMethod: KFunction<*>): MethodLocation {
        return dataProviderLocation.extendWithFunction(builderDataMethod)
    }

    private fun builderDataMethodDataType(builderDataMethod: KFunction<*>): KType {
        return builderDataMethod.returnType
    }

    private fun isIgnoreNullValue(method: KFunction<*>): Boolean {
        return method.hasAnnotation<IgnoreProvidedNullFacetValue>()
    }

    fun getBuilderDataMethods(): List<KFunction<*>> {
        return RelevantMethodFetcher.filterOwnFunctions(dataProviderClass.declaredFunctions)
            .filter { it.hasAnnotation<BuilderData>() }
    }
}