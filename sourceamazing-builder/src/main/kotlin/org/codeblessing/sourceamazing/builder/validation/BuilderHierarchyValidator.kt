package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.factories.BuilderFactoriesHolder
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.BuilderRedeclarationHelper
import org.codeblessing.sourceamazing.builder.utils.RelevantMethodFetcher
import org.codeblessing.sourceamazing.builder.validation.BuilderClassValidator.validateBuilderClass
import org.codeblessing.sourceamazing.builder.validation.BuilderMethodValidator.validateBuilderMethod
import org.codeblessing.sourceamazing.builder.validation.NonBuilderMethodValidator.validateNonBuilderMethod
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

object BuilderHierarchyValidator {

    fun validateTopLevelBuilderMethods(
        builderClass: KClass<*>,
        builderFactoriesHolder: BuilderFactoriesHolder,
        schemaAccess: TypeSafeSchemaAccess,
        superiorClazzes: Map<Alias, Clazz>,
    ) {
        val validationData =
            createRootValidationData(builderClass, builderFactoriesHolder, schemaAccess, superiorClazzes)
        validateBuilderClassStructureAndMethodSyntax(validationData)
    }

    /** This method is called by recursion. */
    private fun validateBuilderClassStructureAndMethodSyntax(builderClassValidationData: BuilderClassValidationData) {
        validateBuilderClassSyntax(builderClassValidationData)

        forEachMethod(builderClassValidationData) { method ->
            builderClassValidationData.ifNotAlreadyProcessed(method) {
                val builderMethodValidationData = builderClassValidationData.builderMethod(method)

                if (builderMethodValidationData.isBuilderMethod()) {
                    validateBuilderMethodSyntax(builderMethodValidationData)
                    val subBuilderClass = validateSubBuilderClassAndGet(builderMethodValidationData)
                    if (subBuilderClass != null) {
                        val subBuilderClassValidationData = builderMethodValidationData.subBuilderClass(subBuilderClass)
                        validateBuilderClassStructureAndMethodSyntax(subBuilderClassValidationData)
                    }
                } else {
                    validateNonBuilderMethodSyntax(builderMethodValidationData)
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

    private fun validateNonBuilderMethodSyntax(builderMethodValidationData: BuilderMethodValidationData) {
        validateNonBuilderMethod(
            builderMethodInterpreter = builderMethodValidationData.builderMethodInterpreter,
            builderFactoriesHolder = builderMethodValidationData.builderClassValidationData.builderFactoriesHolder,
        )
    }

    private fun validateSubBuilderClassAndGet(builderMethodValidationData: BuilderMethodValidationData): KClass<*>? {
        val builderMethodReturnType = builderMethodValidationData.builderMethodReturnType()

        if (builderMethodReturnType.hasNoSubBuilder()) {
            return null
        }

        return builderMethodReturnType.subBuilderClass()
    }

    private fun createRootValidationData(
        builderClass: KClass<*>,
        builderFactoriesHolder: BuilderFactoriesHolder,
        schemaAccess: TypeSafeSchemaAccess,
        superiorClazzes: Map<Alias, Clazz>,
    ): BuilderClassValidationData {
        return BuilderClassValidationData(
            builderClass = builderClass,
            builderFactoriesHolder = builderFactoriesHolder,
            builderClassInterpreter =
                BuilderClassInterpreter(
                    builderClass = builderClass,
                    newClazzesWithAliasFromSuperiorBuilder = superiorClazzes,
                ),
            schemaAccess = schemaAccess,
            recursionDetector = RecursionDetector(),
        )
    }

    private class BuilderClassValidationData(
        val builderClass: KClass<*>,
        val builderFactoriesHolder: BuilderFactoriesHolder,
        val builderClassInterpreter: BuilderClassInterpreter,
        val schemaAccess: TypeSafeSchemaAccess,
        val recursionDetector: RecursionDetector,
    ) {
        fun builderMethod(method: KFunction<*>): BuilderMethodValidationData {
            return BuilderMethodValidationData(method = method, builderClassValidationData = this)
        }

        fun ifNotAlreadyProcessed(method: KFunction<*>, block: () -> Unit) {
            val expectedClazzesFromSuperiorBuilder: Map<Alias, Clazz> =
                builderClassInterpreter.newClazzesFromSuperiorBuilderFilteredByExpectedAliases()
            val isNotProcessed = recursionDetector.pushMethodOntoStack(method, expectedClazzesFromSuperiorBuilder)
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

        fun isBuilderMethod(): Boolean {
            return builderMethodInterpreter.isBuilderMethod()
        }

        fun subBuilderClass(subBuilderClass: KClass<*>): BuilderClassValidationData {
            val expectedClazzesFromSuperiorMethod: Map<Alias, Clazz> =
                builderClassValidationData.builderClassInterpreter
                    .newClazzesFromSuperiorBuilderFilteredByExpectedAliases()

            val newClazzesWithAliasFromSuperiorBuilder =
                BuilderRedeclarationHelper.mapRedeclarations(
                    sourceAliasMap = expectedClazzesFromSuperiorMethod + builderMethodInterpreter.newClazzes(),
                    aliasRedeclarations = builderMethodInterpreter.aliasRedeclarations(),
                )
            val subBuilderClassInterpreter =
                BuilderClassInterpreter(
                    builderClass = subBuilderClass,
                    newClazzesWithAliasFromSuperiorBuilder = newClazzesWithAliasFromSuperiorBuilder,
                )
            return BuilderClassValidationData(
                builderClass = subBuilderClass,
                builderFactoriesHolder = builderClassValidationData.builderFactoriesHolder,
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
            throw BuilderMethodSyntaxException(
                builderMethodInterpreter.methodLocation,
                errorCode.withFormattedMessage(),
            )
        }
    }
}
