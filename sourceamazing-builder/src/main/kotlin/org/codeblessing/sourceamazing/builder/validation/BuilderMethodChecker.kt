package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationBaseData
import org.codeblessing.sourceamazing.builder.validation.BuilderAliasHelper.firstDuplicateAlias
import org.codeblessing.sourceamazing.builder.validation.BuilderAliasHelper.firstMissingAlias
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.utils.type.returnTypeOrNull

class BuilderMethodChecker(
    private val methodToInspect: KFunction<*>,
    private val methodLocation: MethodLocation,
    private val schemaAccess: SchemaAccess,
    private val conceptNameByAliasResolver: (Alias) -> ConceptName,
) {

    fun checkHasBuilderMethodAnnotation() {
        if (!methodToInspect.hasAnnotation<BuilderMethod>()) {
            throw BuilderMethodSyntaxException(methodLocation, BuilderErrorCode.MISSING_BUILDER_ANNOTATION)
        }
    }

    fun checkNoDuplicateAliasInNewConceptAnnotation(allUsedAliasesIncludingDuplicates: List<Alias>) {
        val duplicateAlias = firstDuplicateAlias(allUsedAliasesIncludingDuplicates)

        if (duplicateAlias != null) {
            val concept = conceptNameByAliasResolver(duplicateAlias)
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.ALIAS_IS_ALREADY_USED,
                duplicateAlias,
                concept.clazz.longText(),
                allUsedAliasesIncludingDuplicates.toSet(),
            )
        }
    }

    fun checkNoDuplicateSetRandomConceptIdentifierAliases(setRandomConceptIdentifierAliases: List<Alias>) {
        val duplicateRandomConceptIdentifierAlias = firstDuplicateAlias(setRandomConceptIdentifierAliases)
        if (duplicateRandomConceptIdentifierAlias != null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE,
                duplicateRandomConceptIdentifierAlias,
            )
        }
    }

    fun checkNoDuplicateSetConceptIdentifierAliases(setConceptIdentifierValueAliases: List<Alias>) {
        val duplicateSetConceptIdentifierValueAlias = firstDuplicateAlias(setConceptIdentifierValueAliases)

        if (duplicateSetConceptIdentifierValueAlias != null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.DUPLICATE_SET_CONCEPT_IDENTIFIER_VALUE_USAGE,
                duplicateSetConceptIdentifierValueAlias,
            )
        }
    }

    fun checkNoDuplicateConceptIdentifierOverAllAliases(allConceptIdentifierAssignmentAliases: List<Alias>) {
        val duplicateAssignmentAlias = firstDuplicateAlias(allConceptIdentifierAssignmentAliases)

        if (duplicateAssignmentAlias != null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.DUPLICATE_CONCEPT_IDENTIFIER_INITIALIZATION,
                duplicateAssignmentAlias,
            )
        }
    }

    fun checkNoMissingAliasInNewConceptAnnotations(
        aliasesFromNewConceptAssignment: List<Alias>,
        allConceptIdentifierAssignmentAliases: List<Alias>,
    ) {
        val missingConceptIdentifierAssignment =
            firstMissingAlias(aliasesFromNewConceptAssignment, allConceptIdentifierAssignmentAliases)
        if (missingConceptIdentifierAssignment != null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER,
                missingConceptIdentifierAssignment,
            )
        }
    }

    fun checkNoMissingAliasInSetRandomConceptIdentifierAnnotations(
        setRandomConceptIdentifierAliases: List<Alias>,
        aliasesFromNewConceptAssignment: List<Alias>,
    ) {
        firstMissingAlias(setRandomConceptIdentifierAliases, aliasesFromNewConceptAssignment)?.let { unknownAlias ->
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.UNKNOWN_ALIAS,
                unknownAlias,
                SetRandomConceptIdentifierValue::class.annotationText(),
                aliasesFromNewConceptAssignment,
            )
        }
    }

    fun checkNoMissingAliasInSetConceptIdentifierAnnotations(
        setConceptIdentifierValueAliases: List<Alias>,
        aliasesFromNewConceptAssignment: List<Alias>,
    ) {
        firstMissingAlias(setConceptIdentifierValueAliases, aliasesFromNewConceptAssignment)?.let { unknownAlias ->
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.UNKNOWN_ALIAS,
                unknownAlias,
                SetConceptIdentifierValue::class.annotationText(),
                aliasesFromNewConceptAssignment,
            )
        }
    }

    private fun builderReturnType(): KTypeUtil.KTypeClassInformation {
        val classesInformationFromKType =
            try {
                KTypeUtil.classesInformationFromKType(methodToInspect.returnType)
            } catch (ex: IllegalStateException) {
                throw BuilderMethodSyntaxException(
                    methodLocation,
                    BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS,
                    ex.message ?: "",
                )
            }
        if (classesInformationFromKType.size != 1) {
            throw BuilderMethodSyntaxException(methodLocation, BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS, "")
        }

        return classesInformationFromKType.first()
    }

    fun checkBuilderMethodReturnTypeIsUnitOrBuilderClass() {
        if (methodToInspect.returnTypeOrNull() == null) {
            return
        }
        builderReturnType()
    }

    fun checkBuilderMethodReturnTypeIsUnitOrNotNullable() {
        if (methodToInspect.returnTypeOrNull() == null) {
            return
        }
        val classInformation = builderReturnType()
        if (classInformation.isValueNullable) {
            throw BuilderMethodSyntaxException(methodLocation, BuilderErrorCode.BUILDER_RETURNED_CAN_NOT_BE_NULLABLE)
        }
    }

    fun checkIsKnownConcept(alias: Alias, conceptName: ConceptName) {
        if (!schemaAccess.hasConceptName(conceptName)) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.UNKNOWN_CONCEPT,
                alias,
                conceptName.clazz.longText(),
            )
        }
    }

    private fun checkIsValidAlias(
        annotationBaseData: FacetValueAnnotationBaseData,
        alias: Alias,
        knownConceptAliases: Map<Alias, ConceptName>,
    ) {

        if (alias !in knownConceptAliases) {
            val annotation = annotationBaseData.annotation

            throw BuilderMethodSyntaxException(
                methodLocation = annotationBaseData.methodLocation,
                errorCode = BuilderErrorCode.UNKNOWN_ALIAS,
                alias.name,
                annotation.annotationClass.annotationText(),
                knownConceptAliases.keys,
            )
        }
    }
}
