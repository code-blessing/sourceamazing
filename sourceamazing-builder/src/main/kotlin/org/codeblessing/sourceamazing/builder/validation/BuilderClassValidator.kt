package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.defaultAliasHint
import org.codeblessing.sourceamazing.builder.alias.BuilderAliasHelper.firstDuplicateAlias
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderClassInterpreter
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasAnnotation
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasExactNumberOfAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoExtensionFunctions
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoGenericTypeParameters
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoProperties
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasOnlyAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkIsOrdinaryInterface

object BuilderClassValidator {
    private const val BUILDER_CLASS_DESCRIPTION = "Builder class"

    fun validateTopLevelBuilderClass(builderClassInterpreter: BuilderClassInterpreter) {
        validateHasOnlyBuilderAnnotation(builderClassInterpreter)  // this is only valid for top-level builder
        validateBuilderClass(builderClassInterpreter)
    }

    fun validateBuilderClass(builderClassInterpreter: BuilderClassInterpreter) {
        validateBuilderClassStructure(builderClassInterpreter)
        validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter)
        validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter)
    }

    private fun validateHasOnlyBuilderAnnotation(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        checkHasOnlyAnnotations(listOf(Builder::class), builderClass, BUILDER_CLASS_DESCRIPTION)
    }

    private fun validateBuilderClassStructure(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        checkIsOrdinaryInterface(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoGenericTypeParameters(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoExtensionFunctions(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasNoProperties(builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasAnnotation(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION)
        checkHasExactNumberOfAnnotations(Builder::class, builderClass, BUILDER_CLASS_DESCRIPTION, numberOf = 1)
        checkHasOnlyAnnotations(listOf(Builder::class, ExpectedAliasFromSuperiorBuilder::class), builderClass, BUILDER_CLASS_DESCRIPTION)
    }

    private fun validateAllExpectedAliasesFromSuperiorBuilderAreProvided(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        val expectedAliases = builderClassInterpreter.expectedAliasesFromSuperiorBuilder()
        val providedAliases = builderClassInterpreter.newConceptAliasesFromSuperiorBuilder()
        expectedAliases.forEach { expectedAlias ->
            if(expectedAlias !in providedAliases) {
                throw BuilderSyntaxException(builderClass, BuilderErrorCode.ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, expectedAlias, defaultAliasHint(expectedAlias))
            }
        }
    }

    private fun validateNoDuplicateAliasInExpectedAliasFromSuperiorBuilder(builderClassInterpreter: BuilderClassInterpreter) {
        val builderClass = builderClassInterpreter.builderClass
        val aliasesIncludingDuplicates = builderClassInterpreter.expectedAliasesFromSuperiorBuilderIncludingDuplicates()
        val duplicateAlias = firstDuplicateAlias(aliasesIncludingDuplicates)

        if(duplicateAlias != null) {
            throw BuilderSyntaxException(builderClass, BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION, duplicateAlias, defaultAliasHint(duplicateAlias))
        }
    }
}