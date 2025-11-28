package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ConceptIdentifierAnnotationData
import org.codeblessing.sourceamazing.builder.validation.BuilderTypeChecker.checkIsClassParameterType
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.utils.type.KTypeUtil

class ConceptIdentifierAnnotationDataChecker(
    private val conceptIdentifierAnnotationData: ConceptIdentifierAnnotationData
) {
    private val methodLocation = conceptIdentifierAnnotationData.methodLocation

    fun checkNoConceptIdentifierAnnotationAndIgnoreNullValueAnnotationTogether() {
        if (conceptIdentifierAnnotationData.ignoreNullValue) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_AND_IGNORE_NULL_ANNOTATION,
            )
        }
    }

    private fun checkAndReturnTypeClass(): KTypeUtil.KTypeClassInformation {
        val typeClasses =
            checkIsClassParameterType(
                conceptIdentifierAnnotationData.type,
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_PARAMETER,
            )
        if (typeClasses.size != 1) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
                conceptIdentifierAnnotationData.type,
            )
        }
        return typeClasses.first()
    }

    fun checkConceptIdentifierIsOrdinaryClass() {
        checkAndReturnTypeClass()
    }

    fun checkConceptIdentifierType() {
        val typeClass = checkAndReturnTypeClass()
        if (typeClass.clazz != ConceptIdentifier::class) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_WRONG_CONCEPT_IDENTIFIER_TYPE,
                typeClass.clazz.longText(),
            )
        }
    }

    fun checkConceptIdentifierIsNotNullable() {
        val typeClass = checkAndReturnTypeClass()

        if (typeClass.isValueNullable) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_CONCEPT_IDENTIFIER_TYPE_NO_NULLABLE,
            )
        }
    }
}
