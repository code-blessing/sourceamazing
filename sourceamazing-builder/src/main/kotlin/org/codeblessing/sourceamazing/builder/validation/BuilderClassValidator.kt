package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter

object BuilderClassValidator {

    fun validateBuilderClass(builderClassInterpreter: BuilderClassInterpreter) {
        with(builderClassChecker(builderClassInterpreter)) {
            validateBuilderClassStructure()
            validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter)
            validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter)
            validateAllExpectedAliasesFromSuperiorBuilderHaveMatchingConcepts(builderClassInterpreter)
        }
    }

    private fun BuilderClassChecker.validateBuilderClassStructure() {
        checkIsOrdinaryInterface()
        checkHasNoGenericTypeParameters()
        checkHasNoExtensionFunctions()
        checkHasNoProperties()
        checkHasAnnotation(Builder::class)
        checkHasExactNumberOfAnnotations(Builder::class, numberOf = 1)
        checkHasOnlyAnnotations(listOf(Builder::class, ExpectedAliasFromSuperiorBuilder::class))
    }

    private fun BuilderClassChecker.validateAllExpectedAliasesFromSuperiorBuilderAreProvided(
        builderClassInterpreter: BuilderClassInterpreter
    ) {
        val expectedAliases = builderClassInterpreter.expectedAliasesFromSuperiorBuilder()
        val providedAliases = builderClassInterpreter.newConceptAliasesFromSuperiorBuilder()
        checkAllExpectedAliasesAreProvided(expectedAliases, providedAliases)
    }

    private fun BuilderClassChecker.validateAllExpectedAliasesFromSuperiorBuilderHaveMatchingConcepts(
        builderClassInterpreter: BuilderClassInterpreter
    ) {
        val expectedAliasesAndConcepts = builderClassInterpreter.expectedAliasesAndConceptNamesFromSuperiorBuilder()
        val providedAliasesAndConcepts =
            builderClassInterpreter.newConceptNamesFromSuperiorBuilderFilteredByExpectedAliases()
        checkAllExpectedAliasesAreMatchingProvidedAliasOnConceptNames(
            expectedAliases = expectedAliasesAndConcepts,
            providedAliases = providedAliasesAndConcepts,
        )
    }

    private fun BuilderClassChecker.validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(
        builderClassInterpreter: BuilderClassInterpreter
    ) {
        val aliasesIncludingDuplicates = builderClassInterpreter.expectedAliasesFromSuperiorBuilderIncludingDuplicates()
        checkNoDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation(aliasesIncludingDuplicates)
    }

    private fun builderClassChecker(builderClassInterpreter: BuilderClassInterpreter): BuilderClassChecker {
        return BuilderClassChecker(builderClassInterpreter.builderClass, "Builder class")
    }
}
