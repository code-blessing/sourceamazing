package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

object BuilderMethodValidator {

    fun validateBuilderMethod(builderMethodInterpreter: BuilderMethodInterpreter, schemaAccess: TypeSafeSchemaAccess) {
        val builderMethod = builderMethodValidationData(builderMethodInterpreter, schemaAccess)

        validateHasBuilderMethodAnnotation(builderMethod)
        validateBuilderMethodParameters(builderMethod)

        validateNoDuplicateAliasInNewClazzAnnotation(builderMethod)
        validateKnownClazzesFromNewClazzAnnotation(builderMethod)
        validateNewClazzCanBeInstantiated(builderMethod)
        validateOnlyKnownRedeclarationAliases(builderMethod)
        validateClazzModelIdAssignment(builderMethod)
        validateClazzProperties(builderMethod)

        validateCorrectClazzModelIdTypes(builderMethod)
        validateBuilderMethodReturnType(builderMethod)
    }

    private fun validateHasBuilderMethodAnnotation(builderMethod: BuilderMethodValidationData) {
        with(builderMethodChecker(builderMethod)) { checkHasBuilderMethodAnnotation() }
    }

    private fun validateBuilderMethodParameters(builderMethod: BuilderMethodValidationData) {
        BuilderMethodParameterValidator.validateBuilderMethodParameter(builderMethod.builderMethodInterpreter)
    }

    private fun validateNoDuplicateAliasInNewClazzAnnotation(builderMethod: BuilderMethodValidationData) {
        val builderMethodInterpreter = builderMethod.builderMethodInterpreter
        val builderClassInterpreter = builderMethodInterpreter.builderClassInterpreter
        with(builderMethodChecker(builderMethod)) {
            val aliasesFromSuperiorBuilder = builderClassInterpreter.expectedAliasesFromSuperiorBuilder().toList()
            val allAliasesFromNewClazzAnnotations = builderMethodInterpreter.newClazzAliasesIncludingDuplicates()
            val allUsedAliasesIncludingDuplicates = aliasesFromSuperiorBuilder + allAliasesFromNewClazzAnnotations

            checkNoDuplicateAliasInNewClazzAnnotation(allUsedAliasesIncludingDuplicates)
        }
    }

    private fun validateOnlyKnownRedeclarationAliases(builderMethod: BuilderMethodValidationData) {
        val builderMethodInterpreter = builderMethod.builderMethodInterpreter
        val builderClassInterpreter = builderMethodInterpreter.builderClassInterpreter
        with(builderMethodChecker(builderMethod)) {
            val aliasesFromSuperiorBuilder = builderClassInterpreter.expectedAliasesFromSuperiorBuilder()
            val allAliasesFromNewClazzAnnotations = builderMethodInterpreter.newClazzAliases()
            val allUsedAliases = aliasesFromSuperiorBuilder + allAliasesFromNewClazzAnnotations

            checkOnlyKnownAliasesForRedeclaration(allUsedAliases, builderMethodInterpreter.aliasRedeclarations())
        }
    }

    private fun validateKnownClazzesFromNewClazzAnnotation(builderMethod: BuilderMethodValidationData) {
        with(builderMethodChecker(builderMethod)) {
            builderMethod.builderMethodInterpreter.newClazzes().forEach { (clazzAlias, clazz) ->
                checkIsKnownClazz(clazzAlias, clazz)
            }
        }
    }

    private fun validateNewClazzCanBeInstantiated(builderMethod: BuilderMethodValidationData) {
        with(builderMethodChecker(builderMethod)) {
            builderMethod.builderMethodInterpreter.newClazzes().forEach { (clazzAlias, clazz) ->
                checkIsInstantiableClazz(clazzAlias, clazz)
            }
        }
    }

    private fun validateClazzModelIdAssignment(builderMethod: BuilderMethodValidationData) {
        val builderMethodInterpreter = builderMethod.builderMethodInterpreter
        with(builderMethodChecker(builderMethod)) {
            // check no duplicate alias in all @SetAsClazzModelId
            val setClazzModelIdValueAliases =
                builderMethodInterpreter.aliasesToSetClazzModelIdValueAliasesIncludingDuplicates()
            checkNoDuplicateSetClazzModelIdAliases(setClazzModelIdValueAliases)

            val aliasesFromNewClazzAssignment = builderMethodInterpreter.newClazzAliases().toList()
            // check no unknown aliases in @SetAsClazzModelId assignment
            checkNoMissingAliasInSetClazzModelIdAnnotations(setClazzModelIdValueAliases, aliasesFromNewClazzAssignment)
        }
    }

    private fun validateClazzProperties(builderMethod: BuilderMethodValidationData) {
        val knownValidAliases: Map<Alias, Clazz> =
            builderMethod.builderMethodInterpreter.newClazzesAndExpectedClazzesFromSuperiorBuilder()

        forEachClazzProperty(builderMethod) { clazzPropertyChecker ->
            with(clazzPropertyChecker) {
                checkIsValidClazzPropertyAlias(knownValidAliases)
                checkIsKnownClazzProperty(knownValidAliases)
                checkClazzPropertyValue(knownValidAliases)
                checkClazzPropertyValueType(knownValidAliases)
            }
        }
    }

    private fun validateCorrectClazzModelIdTypes(builderMethod: BuilderMethodValidationData) {
        forEachClazzModelId(builderMethod) { clazzModelIdChecker ->
            with(clazzModelIdChecker) {
                checkNoClazzModelIdAnnotationAndIgnoreNullValueAnnotationTogether()
                checkClazzModelIdIsOrdinaryClass()
                checkClazzModelIdIsNotNullable()
            }
        }
    }

    private fun validateBuilderMethodReturnType(builderMethod: BuilderMethodValidationData) {
        with(builderMethodChecker(builderMethod)) {
            checkBuilderMethodReturnTypeIsUnitOrBuilderClass()
            checkBuilderMethodReturnTypeIsUnitOrNotNullable()
        }
    }

    private fun builderMethodChecker(builderMethod: BuilderMethodValidationData): BuilderMethodChecker {
        return BuilderMethodChecker(
            methodToInspect = builderMethod.builderMethodInterpreter.method,
            methodLocation = builderMethod.builderMethodInterpreter.methodLocation,
            schemaAccess = builderMethod.builderMethodInterpreter.schemaAccess,
            clazzByAliasResolver = builderMethod.builderMethodInterpreter::newClazzByAlias,
        )
    }

    private fun builderMethodValidationData(
        builderMethodInterpreter: BuilderMethodInterpreter,
        schemaAccess: TypeSafeSchemaAccess,
    ): BuilderMethodValidationData {
        return BuilderMethodValidationData(builderMethodInterpreter, schemaAccess)
    }

    private class BuilderMethodValidationData(
        val builderMethodInterpreter: BuilderMethodInterpreter,
        val schemaAccess: TypeSafeSchemaAccess,
    )

    private fun forEachClazzModelId(
        builderMethod: BuilderMethodValidationData,
        block: (ClazzModelIdAnnotationDataChecker) -> Unit,
    ) {
        builderMethod.builderMethodInterpreter.getManualAssignedClazzModelIdAnnotationContent().forEach {
            clazzModelIdAnnotationData ->
            val checker = ClazzModelIdAnnotationDataChecker(clazzModelIdAnnotationData)
            block(checker)
        }
    }

    private fun forEachClazzProperty(
        builderMethod: BuilderMethodValidationData,
        block: (ClazzPropertyAnnotationDataChecker) -> Unit,
    ) {
        builderMethod.builderMethodInterpreter.getClazzPropertyValueAnnotationContent().forEach { clazzPropertyValue ->
            val checker = ClazzPropertyValueAnnotationDataChecker(clazzPropertyValue, builderMethod.schemaAccess)
            block(checker)
        }

        builderMethod.builderMethodInterpreter.getClazzPropertyReferenceAnnotationContent().forEach {
            clazzPropertyReference ->
            val checker =
                ClazzPropertyReferenceAnnotationDataChecker(clazzPropertyReference, builderMethod.schemaAccess)
            block(checker)
        }
    }
}
