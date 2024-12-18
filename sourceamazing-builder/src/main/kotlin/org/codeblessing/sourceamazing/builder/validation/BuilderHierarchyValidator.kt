package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.validation.BuilderClassValidator.validateBuilderClass
import org.codeblessing.sourceamazing.builder.validation.BuilderClassValidator.validateTopLevelBuilderClass
import org.codeblessing.sourceamazing.builder.validation.BuilderMethodValidator.validateBuilderMethod
import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.RelevantMethodFetcher
import org.codeblessing.sourceamazing.schema.SchemaAccess
import kotlin.reflect.KClass

object BuilderHierarchyValidator {

    fun validateTopLevelBuilderMethods(topLevelBuilderClass: KClass<*>, schemaAccess: SchemaAccess) {
        val builderClassInterpreter = BuilderClassInterpreter(
            builderClass = topLevelBuilderClass,
            newConceptNamesWithAliasFromSuperiorBuilder = emptyMap(),
        )
        validateTopLevelBuilderClass(builderClassInterpreter)
        validateBuilderClassStructureAndMethodSyntax(builderClassInterpreter, RecursionDetector(), schemaAccess)
    }

    /**
     * This method is called by recursion.
     */
    private fun validateBuilderClassStructureAndMethodSyntax(builderClassInterpreter: BuilderClassInterpreter, recursionDetector: RecursionDetector, schemaAccess: SchemaAccess) {
        validateBuilderClass(builderClassInterpreter)

        val expectedConceptsFromSuperiorBuilder: Map<Alias, ConceptName> = builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()

        RelevantMethodFetcher.ownMemberFunctions(builderClassInterpreter.builderClass).forEach { method ->
            if(recursionDetector.pushMethodOntoStack(method, expectedConceptsFromSuperiorBuilder)) {

                val builderMethodInterpreter = BuilderMethodInterpreter(
                    schemaAccess = schemaAccess,
                    builderClassInterpreter = builderClassInterpreter,
                    method = method,
                )
                validateBuilderMethod(builderMethodInterpreter, schemaAccess)

                val expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName> = builderMethodInterpreter.builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()
                val subBuilderClass = validateAndGetSubBuilderClass(builderMethodInterpreter)
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

    private fun validateAndGetSubBuilderClass(builderMethodInterpreter: BuilderMethodInterpreter): KClass<*>? {
        val subBuilderClassFromNewBuilderAnnotation = builderMethodInterpreter.getBuilderClassFromWithNewBuilderAnnotation()
        val subBuilderClassFromReturnType = builderMethodInterpreter.getBuilderClassFromReturnType()
        val subBuilderClassFromInjectBuilderAnnotation = builderMethodInterpreter.getBuilderClassFromInjectBuilderParameter()

        if(subBuilderClassFromReturnType == null
            && subBuilderClassFromInjectBuilderAnnotation == null
            && subBuilderClassFromNewBuilderAnnotation == null) {
            return null
        }

        if(subBuilderClassFromReturnType != null && subBuilderClassFromInjectBuilderAnnotation != null) {
            throw BuilderMethodSyntaxException(builderMethodInterpreter.methodLocation, BuilderErrorCode.BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME)
        }

        if(subBuilderClassFromReturnType == null && subBuilderClassFromInjectBuilderAnnotation == null) {
            throw BuilderMethodSyntaxException(builderMethodInterpreter.methodLocation, BuilderErrorCode.BUILDER_DECLARED_IN_WITH_NEW_BUILDER_ANNOTATION_MUST_BE_USED)
        }

        val subBuilderClass = subBuilderClassFromReturnType ?: subBuilderClassFromInjectBuilderAnnotation


        if(subBuilderClassFromNewBuilderAnnotation != null) {
            if (subBuilderClassFromNewBuilderAnnotation != subBuilderClass) {
                throw BuilderMethodSyntaxException(builderMethodInterpreter.methodLocation, BuilderErrorCode.BUILDER_IN_WITH_NEW_BUILDER_MUST_BE_SAME)
            }
        }

        return subBuilderClass
    }
}