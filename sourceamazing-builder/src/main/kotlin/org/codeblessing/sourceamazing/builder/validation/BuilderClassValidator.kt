package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.firstDuplicateAlias
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedRootAlias
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkHasAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkHasExactNumberOfAnnotations
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkHasNoExtensionFunctions
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkHasNoGenericTypeParameters
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkHasNoProperties
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkHasNotAnnotation
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkHasOnlyAnnotations
import org.codeblessing.sourceamazing.builder.validation.BuilderClassCheckerUtil.checkIsOrdinaryInterface

object BuilderClassValidator {
    private const val BUILDER_CLASS_DESCRIPTION = "Builder class"

    fun validateBuilderClass(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        validateBuilderClassStructure(builderClassInterpreter)
        if(builderClassInterpreter.isTopLevelBuilder) {
            checkHasAnnotation(ExpectedRootAlias::class, builderClass, BUILDER_CLASS_DESCRIPTION)
            checkHasExactNumberOfAnnotations(ExpectedRootAlias::class, builderClass, BUILDER_CLASS_DESCRIPTION, numberOf = 1)
            checkHasNotAnnotation(ExpectedAliasFromSuperiorBuilder::class, builderClass, BUILDER_CLASS_DESCRIPTION)
        } else {
            checkHasNotAnnotation(ExpectedRootAlias::class, builderClass, BUILDER_CLASS_DESCRIPTION)
        }
        validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter)
        validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter)
    }

    private fun validateBuilderClassStructure(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        checkIsOrdinaryInterface(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoGenericTypeParameters(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoExtensionFunctions(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoProperties(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasAnnotation(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasExactNumberOfAnnotations(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION, numberOf = 1)
        checkHasOnlyAnnotations(listOf(Builder::class, ExpectedRootAlias::class, ExpectedAliasFromSuperiorBuilder::class), builderClass, BUILDER_CLASS_DESCRIPTION)
    }

    private fun validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        val expectedAliases = builderClassInterpreter.expectedAliasesFromSuperiorBuilder()
        val providedAliases = builderClassInterpreter.newConceptAliasesFromSuperiorBuilder()
        expectedAliases.forEach { expectedAlias ->
            if(expectedAlias !in providedAliases) {
                throw BuilderSyntaxException(builderClass, BuilderErrorCode.ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, expectedAlias)
            }
        }
    }

    private fun validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        val aliasesIncludingDuplicates = builderClassInterpreter.expectedAliasesFromSuperiorBuilderIncludingDuplicates()
        val duplicateAlias = firstDuplicateAlias(aliasesIncludingDuplicates)

        if(duplicateAlias != null) {
            throw BuilderSyntaxException(builderClass, BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, duplicateAlias)
        }
    }
}
