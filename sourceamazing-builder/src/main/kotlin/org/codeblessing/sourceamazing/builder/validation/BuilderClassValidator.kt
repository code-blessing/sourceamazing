package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter

object BuilderClassValidator {

    fun validateBuilderClass(builderClassInterpreter: BuilderClassInterpreter) {
        with(builderClassChecker(builderClassInterpreter)) {
            validateBuilderClassStructure()
            validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter)
            validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter)
            validateAllExpectedAliasesFromSuperiorBuilderHaveMatchingClazzes(builderClassInterpreter)
        }
    }

    private fun BuilderClassChecker.validateBuilderClassStructure() {
        checkIsOrdinaryInterface()
        checkHasNoGenericTypeParameters()
        checkHasNoExtensionFunctions()
        checkHasNoProperties()
        checkHasAnnotation(Builder::class)
        checkHasExactNumberOfAnnotations(Builder::class, numberOf = 1)
        checkHasOnlyAnnotations(listOf(Builder::class, ExpectedClazzModelFromSuperiorBuilder::class))
    }

    private fun BuilderClassChecker.validateAllExpectedAliasesFromSuperiorBuilderAreProvided(
        builderClassInterpreter: BuilderClassInterpreter
    ) {
        val expectedAliases = builderClassInterpreter.expectedAliasesFromSuperiorBuilder()
        val providedAliases = builderClassInterpreter.newClazzAliasesFromSuperiorBuilder()
        checkAllExpectedAliasesAreProvided(expectedAliases, providedAliases)
    }

    private fun BuilderClassChecker.validateAllExpectedAliasesFromSuperiorBuilderHaveMatchingClazzes(
        builderClassInterpreter: BuilderClassInterpreter
    ) {
        val expectedAliasesAndClazzes = builderClassInterpreter.expectedAliasesAndClazzesFromSuperiorBuilder()
        val providedAliasesAndClazzes = builderClassInterpreter.newClazzesFromSuperiorBuilderFilteredByExpectedAliases()
        checkAllExpectedAliasesAreMatchingProvidedAliasOnClazzes(
            expectedAliases = expectedAliasesAndClazzes,
            providedAliases = providedAliasesAndClazzes,
        )
    }

    private fun BuilderClassChecker.validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(
        builderClassInterpreter: BuilderClassInterpreter
    ) {
        val aliasesIncludingDuplicates = builderClassInterpreter.expectedAliasesFromSuperiorBuilderIncludingDuplicates()
        checkNoDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation(aliasesIncludingDuplicates)
    }

    private fun builderClassChecker(builderClassInterpreter: BuilderClassInterpreter): BuilderClassChecker {
        return BuilderClassChecker(builderClassInterpreter.builderClass)
    }
}
