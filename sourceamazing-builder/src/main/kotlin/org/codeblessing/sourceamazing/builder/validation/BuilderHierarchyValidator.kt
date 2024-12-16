package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateAllExpectedAliasesFromSuperiorBuilderAreProvided
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateBuilderClassStructure
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateBuilderMethodParameter
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateBuilderMethodReturnType
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateConceptIdentifierAssignment
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateCorrectConceptIdentifierTypes
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateCorrectFacetValueTypes
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateCorrectTypesInMethodAnnotations
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateHasBuilderMethodAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateHasOnlyBuilderAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateKnownConceptsFromNewConceptAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateNoDuplicateAliasInNewConceptAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateUsedAliases
import org.codeblessing.sourceamazing.builder.validation.BuilderValidator.validateUsedFacets
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromInjectBuilderParameter
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromReturnType
import org.codeblessing.sourceamazing.builder.validation.SubBuilderHelper.getBuilderClassFromWithNewBuilderAnnotation
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.RelevantMethodFetcher
import org.codeblessing.sourceamazing.schema.SchemaAccess
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

object BuilderHierarchyValidator {

    fun validateTopLevelBuilderMethods(topLevelBuilderClass: KClass<*>, schemaAccess: SchemaAccess) {
        validateHasOnlyBuilderAnnotation(topLevelBuilderClass)
        val builderClassInterpreter = BuilderClassInterpreter(
            builderClass = topLevelBuilderClass,
            newConceptNamesWithAliasFromSuperiorBuilder = emptyMap(),
        )

        validateBuilderClassStructureAndMethodSyntax(builderClassInterpreter, RecursionDetector(), schemaAccess)
    }

    private fun validateBuilderClassStructureAndMethodSyntax(builderClassInterpreter: BuilderClassInterpreter, recursionDetector: RecursionDetector, schemaAccess: SchemaAccess) {
        validateBuilderClassStructure(builderClassInterpreter)
        validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter)
        validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter)

        val expectedConceptsFromSuperiorBuilder: Map<Alias, ConceptName> = builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()

        RelevantMethodFetcher.ownMemberFunctions(builderClassInterpreter.builderClass).forEach { method ->
            if(recursionDetector.pushMethodOntoStack(method, expectedConceptsFromSuperiorBuilder)) {

                validateHasBuilderMethodAnnotation(method)
                val builderMethodInterpreter = BuilderMethodInterpreter(
                    schemaAccess = schemaAccess,
                    builderClassInterpreter = builderClassInterpreter,
                    method = method,
                )
                validateMethodWithBuilderMethodAnnotation(builderMethodInterpreter, schemaAccess)

                val expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName> = builderMethodInterpreter.builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()
                val subBuilderClass = validateAndGetSubBuilderClass(method)
                if(subBuilderClass != null) {
                    val subBuilderClassInterpreter = BuilderClassInterpreter(
                        builderClass = subBuilderClass,
                        newConceptNamesWithAliasFromSuperiorBuilder = expectedConceptsFromSuperiorMethod + builderMethodInterpreter.newConcepts(),
                    )

                    validateBuilderClassStructureAndMethodSyntax(subBuilderClassInterpreter, recursionDetector, schemaAccess)
                }
                recursionDetector.removeLastMethodFromStack()
            }
        }
    }

    private fun validateMethodWithBuilderMethodAnnotation(
        builderMethodInterpreter: BuilderMethodInterpreter,
        schemaAccess: SchemaAccess
    ) {
        val method = builderMethodInterpreter.method

        validateNoDuplicateAliasInNewConceptAnnotation(builderMethodInterpreter)
        validateKnownConceptsFromNewConceptAnnotation(builderMethodInterpreter, schemaAccess)

        validateConceptIdentifierAssignment(builderMethodInterpreter)
        validateUsedAliases(builderMethodInterpreter)
        validateUsedFacets(builderMethodInterpreter, schemaAccess)
        validateCorrectTypesInMethodAnnotations(builderMethodInterpreter, schemaAccess)

        method.valueParameters.forEachIndexed { index, methodParameter ->
            val isLastParameter = index == (method.valueParameters.size - 1)
            validateBuilderMethodParameter(builderMethodInterpreter, methodParameter, schemaAccess, isLastParameter)
        }
        validateCorrectConceptIdentifierTypes(builderMethodInterpreter)
        validateCorrectFacetValueTypes(builderMethodInterpreter, schemaAccess)

        validateBuilderMethodReturnType(builderMethodInterpreter)
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
}