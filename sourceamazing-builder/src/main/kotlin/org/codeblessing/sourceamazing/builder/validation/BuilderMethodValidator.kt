package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaAccess

object BuilderMethodValidator {

    fun validateBuilderMethod(builderMethodInterpreter: BuilderMethodInterpreter, schemaAccess: SchemaAccess) {
        val builderMethod = builderMethodValidationData(builderMethodInterpreter, schemaAccess)

        validateHasBuilderMethodAnnotation(builderMethod)
        validateBuilderMethodParameters(builderMethod)

        validateNoDuplicateAliasInNewConceptAnnotation(builderMethod)
        validateKnownConceptsFromNewConceptAnnotation(builderMethod)
        validateConceptIdentifierAssignment(builderMethod)
        validateFacets(builderMethod)

        validateCorrectConceptIdentifierTypes(builderMethod)
        validateBuilderMethodReturnType(builderMethod)
    }

    private fun validateHasBuilderMethodAnnotation(builderMethod: BuilderMethodValidationData) {
        with(builderMethodChecker(builderMethod)) { checkHasBuilderMethodAnnotation() }
    }

    private fun validateBuilderMethodParameters(builderMethod: BuilderMethodValidationData) {
        BuilderMethodParameterValidator.validateBuilderMethodParameter(
            builderMethod.builderMethodInterpreter,
            builderMethod.schemaAccess,
        )
    }

    private fun validateNoDuplicateAliasInNewConceptAnnotation(builderMethod: BuilderMethodValidationData) {
        val builderMethodInterpreter = builderMethod.builderMethodInterpreter
        val builderClassInterpreter = builderMethodInterpreter.builderClassInterpreter
        with(builderMethodChecker(builderMethod)) {
            val aliasesFromSuperiorBuilder = builderClassInterpreter.expectedAliasesFromSuperiorBuilder().toList()
            val allAliasesFromNewConceptAnnotations = builderMethodInterpreter.newConceptAliasesIncludingDuplicates()
            val allUsedAliasesIncludingDuplicates = aliasesFromSuperiorBuilder + allAliasesFromNewConceptAnnotations

            checkNoDuplicateAliasInNewConceptAnnotation(allUsedAliasesIncludingDuplicates)
        }
    }

    private fun validateKnownConceptsFromNewConceptAnnotation(builderMethod: BuilderMethodValidationData) {
        with(builderMethodChecker(builderMethod)) {
            builderMethod.builderMethodInterpreter.newConcepts().forEach { (conceptAlias, conceptName) ->
                checkIsKnownConcept(conceptAlias, conceptName)
            }
        }
    }

    private fun validateConceptIdentifierAssignment(builderMethod: BuilderMethodValidationData) {
        val builderMethodInterpreter = builderMethod.builderMethodInterpreter
        with(builderMethodChecker(builderMethod)) {
            // check no duplicate alias in all @SetRandomConceptIdentifierValue
            val setRandomConceptIdentifierAliases =
                builderMethodInterpreter.aliasesToSetRandomConceptIdentifierValueIncludingDuplicates()
            checkNoDuplicateSetRandomConceptIdentifierAliases(setRandomConceptIdentifierAliases)

            // check no duplicate alias in all @SetConceptIdentifierValue
            val setConceptIdentifierValueAliases =
                builderMethodInterpreter.aliasesToSetConceptIdentifierValueAliasesIncludingDuplicates()
            checkNoDuplicateSetConceptIdentifierAliases(setConceptIdentifierValueAliases)

            // check no duplicate assignment with @SetRandomConceptIdentifierValue and
            // @SetConceptIdentifierValue
            val allConceptIdentifierAssignmentAliases =
                setRandomConceptIdentifierAliases + setConceptIdentifierValueAliases
            checkNoDuplicateConceptIdentifierOverAllAliases(allConceptIdentifierAssignmentAliases)

            val aliasesFromNewConceptAssignment: List<Alias> = builderMethodInterpreter.newConceptAliases().toList()
            // check no missing assignment for all @NewConcept
            checkNoMissingAliasInNewConceptAnnotations(
                aliasesFromNewConceptAssignment,
                allConceptIdentifierAssignmentAliases,
            )

            // check no unknown aliases in @SetRandomConceptIdentifierValue assignment
            checkNoMissingAliasInSetRandomConceptIdentifierAnnotations(
                setRandomConceptIdentifierAliases,
                aliasesFromNewConceptAssignment,
            )

            // check no unknown aliases in @SetConceptIdentifierValue assignment
            checkNoMissingAliasInSetConceptIdentifierAnnotations(
                setConceptIdentifierValueAliases,
                aliasesFromNewConceptAssignment,
            )
        }
    }

    private fun validateFacets(builderMethod: BuilderMethodValidationData) {
        val knownValidAliases: Map<Alias, ConceptName> =
            builderMethod.builderMethodInterpreter.newConceptNamesAndExpectedConceptNamesFromSuperiorBuilder()

        forEachFacetValue(builderMethod) { facetValueChecker ->
            with(facetValueChecker) {
                checkIsValidFacetAlias(knownValidAliases)
                checkIsValidFacet(knownValidAliases)

                checkIsKnownFacet(knownValidAliases)
                checkFacetType(knownValidAliases)
                checkFacetTypeIfEnum(knownValidAliases)

                checkFacetValueType(knownValidAliases)
            }
        }
    }

    private fun validateCorrectConceptIdentifierTypes(builderMethod: BuilderMethodValidationData) {
        forEachConceptIdentifier(builderMethod) { conceptIdentifierChecker ->
            with(conceptIdentifierChecker) {
                checkNoConceptIdentifierAnnotationAndIgnoreNullValueAnnotationTogether()
                checkConceptIdentifierIsOrdinaryClass()
                checkConceptIdentifierType()
                checkConceptIdentifierIsNotNullable()
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
            conceptNameByAliasResolver = builderMethod.builderMethodInterpreter::newConceptByAlias,
        )
    }

    private fun builderMethodValidationData(
        builderMethodInterpreter: BuilderMethodInterpreter,
        schemaAccess: SchemaAccess,
    ): BuilderMethodValidationData {
        return BuilderMethodValidationData(builderMethodInterpreter, schemaAccess)
    }

    private class BuilderMethodValidationData(
        val builderMethodInterpreter: BuilderMethodInterpreter,
        val schemaAccess: SchemaAccess,
    )

    private fun forEachConceptIdentifier(
        builderMethod: BuilderMethodValidationData,
        block: (ConceptIdentifierAnnotationDataChecker) -> Unit,
    ) {
        builderMethod.builderMethodInterpreter.getManualAssignedConceptIdentifierAnnotationContent().forEach {
            conceptIdentifierAnnotationData ->
            val checker = ConceptIdentifierAnnotationDataChecker(conceptIdentifierAnnotationData)
            block(checker)
        }
    }

    private fun forEachFacetValue(
        builderMethod: BuilderMethodValidationData,
        block: (FacetValueAnnotationDataChecker) -> Unit,
    ) {
        builderMethod.builderMethodInterpreter.getFacetValueAnnotationContent().forEach { facetValue ->
            val checker = FacetValueAnnotationDataChecker(facetValue, builderMethod.schemaAccess)
            block(checker)
        }
    }
}
