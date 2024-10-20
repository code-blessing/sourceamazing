package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.collectNewConceptAliases
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.filterOnlyExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateAllExpectedAliasesFromSuperiorBuilderAreProvided
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateBuilderClassStructure
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateConceptIdentifierAssignment
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateCorrectConceptIdentifierType
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateCorrectFacetValueType
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateCorrectTypesInMethodAnnotations
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateExpectedMethodParameterAnnotations
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateHasBuilderMethodAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateHasOnlyBuilderAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateIgnoreNullFacetValueMethodParameterAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateInjectBuilderMethodParamType
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateInjectBuilderMethodParameterAnnotations
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateKnownConceptsFromNewConceptAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateMethodReturnType
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateNoDuplicateAliasInNewConceptAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateUsedAliasesAndFacets
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromInjectBuilderParameter
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromReturnType
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromWithNewBuilderAnnotation
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.RelevantMethodFetcher
import org.codeblessing.sourceamazing.schema.SchemaAccess
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters

object BuilderHierarchyValidator {

    fun validateTopLevelBuilderMethods(topLevelBuilderClass: KClass<*>, schemaAccess: SchemaAccess) {
        validateHasOnlyBuilderAnnotation(topLevelBuilderClass)
        validateBuilderClassStructureAndMethodSyntax(topLevelBuilderClass, emptyMap(), RecursionDetector(), schemaAccess)
    }

    fun validateBuilderClassStructureAndMethodSyntax(builderClass: KClass<*>, newConceptsFromSuperiorMethod: Map<Alias, ConceptName>, recursionDetector: RecursionDetector, schemaAccess: SchemaAccess) {
        validateBuilderClassStructure(builderClass)
        validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClass)
        validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClass, newConceptsFromSuperiorMethod)

        val expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName> = filterOnlyExpectedAliasFromSuperiorBuilder(builderClass, newConceptsFromSuperiorMethod)
        val expectedAliasesFromSuperiorMethod: Set<Alias> = expectedConceptsFromSuperiorMethod.keys.toSet()

        RelevantMethodFetcher.ownMemberFunctions(builderClass).forEach { method ->
            if(recursionDetector.pushMethodOntoStack(method, expectedConceptsFromSuperiorMethod)) {
                validateHasBuilderMethodAnnotation(method)
                validateNoDuplicateAliasInNewConceptAnnotation(method, expectedAliasesFromSuperiorMethod)
                validateKnownConceptsFromNewConceptAnnotation(method, schemaAccess)

                val newConceptsFromMethod: Map<Alias, ConceptName> = collectNewConceptAliases(method)

                validateConceptIdentifierAssignment(method, newConceptsFromMethod.keys)
                validateUsedAliasesAndFacets(method, newConceptsFromMethod + expectedConceptsFromSuperiorMethod, schemaAccess)
                validateCorrectTypesInMethodAnnotations(method, schemaAccess)

                method.valueParameters.forEachIndexed { index, methodParameter ->
                    val isLastParameter = index == (method.valueParameters.size - 1)
                    validateBuilderMethodParameter(method, methodParameter, schemaAccess, isLastParameter)
                }

                validateMethodReturnType(method)

                val subBuilderClass = validateAndGetSubBuilderClass(method)
                if(subBuilderClass != null) {
                    validateBuilderClassStructureAndMethodSyntax(subBuilderClass, expectedConceptsFromSuperiorMethod + newConceptsFromMethod, recursionDetector, schemaAccess)
                }
                recursionDetector.removeLastMethodFromStack()
            }
        }
    }

    private fun validateAndGetSubBuilderClass(method: KFunction<*>): KClass<*>? {
        val subBuilderClassFromNewBuilderAnnotation = getBuilderClassFromWithNewBuilderAnnotation(method)
        val subBuilderClassFromReturnType = getBuilderClassFromReturnType(method)
        val subBuilderClassFromInjectBuilderAnnotation = getBuilderClassFromInjectBuilderParameter(method)

        if(subBuilderClassFromReturnType == null
            && subBuilderClassFromInjectBuilderAnnotation == null
            && subBuilderClassFromNewBuilderAnnotation == null) {
            return null
        }

        if(subBuilderClassFromReturnType != null && subBuilderClassFromInjectBuilderAnnotation != null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME)
        }

        if(subBuilderClassFromReturnType == null && subBuilderClassFromInjectBuilderAnnotation == null) {
            throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_DECLARED_IN_WITH_NEW_BUILDER_ANNOTATION_MUST_BE_USED)
        }

        val subBuilderClass = subBuilderClassFromReturnType ?: subBuilderClassFromInjectBuilderAnnotation


        if(subBuilderClassFromNewBuilderAnnotation != null) {
            if (subBuilderClassFromNewBuilderAnnotation != subBuilderClass) {
                throw BuilderMethodSyntaxException(method, BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME)
            }
        }

        return subBuilderClass
    }



    private fun validateBuilderMethodParameter(method: KFunction<*>, methodParameter: KParameter, schemaAccess: SchemaAccess, isLastParameter: Boolean) {
        validateInjectBuilderMethodParameterAnnotations(method, methodParameter, isLastParameter)
        validateExpectedMethodParameterAnnotations(method, methodParameter, isLastParameter)
        validateInjectBuilderMethodParamType(method, methodParameter)
        validateIgnoreNullFacetValueMethodParameterAnnotation(method, methodParameter)
        validateCorrectParameterTypes(method, methodParameter, schemaAccess)
    }

    private fun validateCorrectParameterTypes(
        method: KFunction<*>,
        methodParameter: KParameter,
        schemaAccess: SchemaAccess
    ) {
        if(methodParameter.hasAnnotation<InjectBuilder>()) {
            return
        }
        if(methodParameter.hasAnnotation<SetConceptIdentifierValue>()) {
            validateCorrectConceptIdentifierType(method, methodParameter)
            return
        }
        if(methodParameter.hasAnnotation<SetFacetValue>()) {
            validateCorrectFacetValueType(method, methodParameter, schemaAccess)
            return
        }
    }
}