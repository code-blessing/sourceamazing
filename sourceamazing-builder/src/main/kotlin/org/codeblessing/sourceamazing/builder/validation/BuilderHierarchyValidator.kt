package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.BuilderErrorCode.BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.validation.BuilderClassValidator.validateBuilderClass
import org.codeblessing.sourceamazing.builder.validation.BuilderMethodValidator.validateBuilderMethod
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.utils.RelevantMethodFetcher
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object BuilderHierarchyValidator {

    fun validateTopLevelBuilderMethods(
        builderClass: KClass<*>,
        schemaAccess: SchemaAccess,
        superiorConcepts: Map<Alias, ConceptName>,
    ) {
        val validationData = createRootValidationData(builderClass, schemaAccess, superiorConcepts)
        validateBuilderClassStructureAndMethodSyntax(validationData)
    }

    /** This method is called by recursion. */
    private fun validateBuilderClassStructureAndMethodSyntax(builderClassValidationData: BuilderClassValidationData) {
        validateBuilderClassSyntax(builderClassValidationData)

        forEachMethod(builderClassValidationData) { method ->
            builderClassValidationData.ifNotAlreadyProcessed(method) {
                val builderMethodValidationData = builderClassValidationData.builderMethod(method)
                validateBuilderMethodSyntax(builderMethodValidationData)

                val subBuilderClass = validateSubBuilderClassAndGet(builderMethodValidationData)
                if (subBuilderClass != null) {
                    val subBuilderClassValidationData = builderMethodValidationData.subBuilderClass(subBuilderClass)
                    validateBuilderClassStructureAndMethodSyntax(subBuilderClassValidationData)
                }
            }
        }
    }

    private fun validateBuilderClassSyntax(builderClassValidationData: BuilderClassValidationData) {
        validateBuilderClass(builderClassValidationData.builderClassInterpreter)
    }

    private fun forEachMethod(
        builderClassValidationData: BuilderClassValidationData,
        block: (method: KFunction<*>) -> Unit,
    ) {
        RelevantMethodFetcher.ownMemberFunctions(builderClassValidationData.builderClass).forEach { method ->
            block(method)
        }
    }

    private fun validateBuilderMethodSyntax(builderMethodValidationData: BuilderMethodValidationData) {
        validateBuilderMethod(
            builderMethodInterpreter = builderMethodValidationData.builderMethodInterpreter,
            schemaAccess = builderMethodValidationData.builderClassValidationData.schemaAccess,
        )
    }

    private fun validateSubBuilderClassAndGet(builderMethodValidationData: BuilderMethodValidationData): KClass<*>? {
        val builderMethodReturnType = builderMethodValidationData.builderMethodReturnType()

        if (builderMethodReturnType.hasNoSubBuilder()) {
            return null
        }

        if (builderMethodReturnType.hasReturnTypeAndBuilderClassInjectAnnotation()) {
            builderMethodReturnType.throwWithBuilderErrorCode(BUILDER_INJECTION_AND_RETURN_AT_SAME_TIME)
        }

        return builderMethodReturnType.subBuilderClass()
    }

    private fun createRootValidationData(
        builderClass: KClass<*>,
        schemaAccess: SchemaAccess,
        superiorConcepts: Map<Alias, ConceptName>,
    ): BuilderClassValidationData {
        return BuilderClassValidationData(
            builderClass = builderClass,
            builderClassInterpreter =
                BuilderClassInterpreter(
                    builderClass = builderClass,
                    newConceptNamesWithAliasFromSuperiorBuilder = superiorConcepts,
                ),
            schemaAccess = schemaAccess,
            recursionDetector = RecursionDetector(),
        )
    }

    private class BuilderClassValidationData(
        val builderClass: KClass<*>,
        val builderClassInterpreter: BuilderClassInterpreter,
        val schemaAccess: SchemaAccess,
        val recursionDetector: RecursionDetector,
    ) {
        fun builderMethod(method: KFunction<*>): BuilderMethodValidationData {
            return BuilderMethodValidationData(method = method, builderClassValidationData = this)
        }

        fun ifNotAlreadyProcessed(method: KFunction<*>, block: () -> Unit) {
            val expectedConceptsFromSuperiorBuilder: Map<Alias, ConceptName> =
                builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()
            val isNotProcessed = recursionDetector.pushMethodOntoStack(method, expectedConceptsFromSuperiorBuilder)
            if (isNotProcessed) {
                block()
                recursionDetector.removeLastMethodFromStack()
            }
        }
    }

    private class BuilderMethodValidationData(
        val method: KFunction<*>,
        val builderClassValidationData: BuilderClassValidationData,
    ) {
        val builderMethodInterpreter: BuilderMethodInterpreter =
            BuilderMethodInterpreter(
                schemaAccess = builderClassValidationData.schemaAccess,
                builderClassInterpreter = builderClassValidationData.builderClassInterpreter,
                method = method,
            )

        fun subBuilderClass(subBuilderClass: KClass<*>): BuilderClassValidationData {
            val expectedConceptsFromSuperiorMethod: Map<Alias, ConceptName> =
                builderClassValidationData.builderClassInterpreter
                    .newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()

            val subBuilderClassInterpreter =
                BuilderClassInterpreter(
                    builderClass = subBuilderClass,
                    newConceptNamesWithAliasFromSuperiorBuilder =
                        expectedConceptsFromSuperiorMethod + builderMethodInterpreter.newConcepts(),
                )
            return BuilderClassValidationData(
                builderClass = subBuilderClass,
                builderClassInterpreter = subBuilderClassInterpreter,
                schemaAccess = builderClassValidationData.schemaAccess,
                recursionDetector = builderClassValidationData.recursionDetector,
            )
        }

        fun builderMethodReturnType(): BuilderMethodReturnTypeValidationData {
            val subBuilderClassFromReturnType = builderMethodInterpreter.getBuilderClassFromReturnType()
            val subBuilderClassFromInjectBuilderAnnotation =
                builderMethodInterpreter.getBuilderClassFromInjectBuilderParameter()

            return BuilderMethodReturnTypeValidationData(
                builderMethodInterpreter = builderMethodInterpreter,
                subBuilderClassFromReturnType = subBuilderClassFromReturnType,
                subBuilderClassFromInjectBuilderAnnotation = subBuilderClassFromInjectBuilderAnnotation,
            )
        }
    }

    private class BuilderMethodReturnTypeValidationData(
        private val builderMethodInterpreter: BuilderMethodInterpreter,
        private val subBuilderClassFromReturnType: KClass<*>?,
        private val subBuilderClassFromInjectBuilderAnnotation: KClass<*>?,
    ) {
        fun hasNoSubBuilder(): Boolean {
            return subBuilderClassFromReturnType == null && subBuilderClassFromInjectBuilderAnnotation == null
        }

        fun hasReturnTypeAndBuilderClassInjectAnnotation(): Boolean {
            return subBuilderClassFromReturnType != null && subBuilderClassFromInjectBuilderAnnotation != null
        }

        fun subBuilderClass(): KClass<*> {
            return requireNotNull(subBuilderClassFromReturnType ?: subBuilderClassFromInjectBuilderAnnotation)
        }

        fun throwWithBuilderErrorCode(errorCode: BuilderErrorCode): Nothing {
            throw BuilderMethodSyntaxException(builderMethodInterpreter.methodLocation, errorCode)
        }
    }
}
