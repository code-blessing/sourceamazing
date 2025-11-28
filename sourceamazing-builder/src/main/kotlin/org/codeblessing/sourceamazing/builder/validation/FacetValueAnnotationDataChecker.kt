package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderCollectionHelper.SUPPORTED_COLLECTION_TYPES
import org.codeblessing.sourceamazing.builder.interpretation.BuilderCollectionHelper.extractValueClassFromCollectionIfCollection
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.EnumFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ReferenceFacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.validation.BuilderTypeChecker.checkIsClassParameterType
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.utils.enumeration.EnumUtil
import org.codeblessing.sourceamazing.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.utils.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.utils.type.isEnum

class FacetValueAnnotationDataChecker(
    private val facetValueAnnotationContent: FacetValueAnnotationContent,
    private val schemaAccess: SchemaAccess,
) {
    fun checkIsValidFacetAlias(knownValidAliases: Map<Alias, ConceptName>) {
        validateIsValidAlias(facetValueAnnotationContent.base.alias, knownValidAliases)
        if (
            facetValueAnnotationContent is ReferenceFacetValueAnnotationContent &&
                facetValueAnnotationContent.referencedAlias != null
        ) {
            validateIsValidAlias(facetValueAnnotationContent.referencedAlias, knownValidAliases)
        }
    }

    private fun validateIsValidAlias(alias: Alias, knownConceptAliases: Map<Alias, ConceptName>) {
        val annotationBaseData = facetValueAnnotationContent.base

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

    fun checkIsValidFacet(knownConceptAliases: Map<Alias, ConceptName>) {
        val annotation = facetValueAnnotationContent.base.annotation
        val alias = facetValueAnnotationContent.base.alias
        val facetName = facetValueAnnotationContent.base.facetName

        val conceptName = requireNotNull(knownConceptAliases[alias]) { "Concept for ${alias.name} does not exist" }
        val concept = schemaAccess.conceptByConceptName(conceptName)
        if (!concept.hasFacet(facetName)) {
            throw BuilderMethodSyntaxException(
                methodLocation = facetValueAnnotationContent.base.methodLocation,
                errorCode = BuilderErrorCode.UNKNOWN_FACET,
                annotation.annotationClass.annotationText(),
                facetName.longText(),
            )
        }
    }

    fun checkIsKnownFacet() {
        checkKnownFacetAndReturn()
    }

    private fun checkKnownFacetAndReturn(): FacetSchema {
        val annotationBaseData = facetValueAnnotationContent.base
        val annotation: Annotation = annotationBaseData.annotation
        val facetName = annotationBaseData.facetName

        return schemaAccess.facetByFacetName(facetName)
            ?: throw BuilderMethodSyntaxException(
                annotationBaseData.methodLocation,
                BuilderErrorCode.UNKNOWN_FACET,
                annotation.annotationClass.annotationText(),
                facetName.longText(),
            )
    }

    fun checkFacetType() {
        val annotationBaseData = facetValueAnnotationContent.base
        val expectedFacetType = facetValueAnnotationContent.expectedFacetType

        val annotation: Annotation = annotationBaseData.annotation
        val facetName = annotationBaseData.facetName

        val facet = checkKnownFacetAndReturn()

        if (expectedFacetType != null && facet.facetType != expectedFacetType) {
            throw BuilderMethodSyntaxException(
                annotationBaseData.methodLocation,
                BuilderErrorCode.WRONG_FACET_TYPE,
                annotation.annotationClass.annotationText(),
                facetName.longText(),
                expectedFacetType,
                facet.facetType,
            )
        }
    }

    fun checkFacetTypeIfEnum() {
        if (facetValueAnnotationContent is EnumFacetValueAnnotationContent) {
            checkFacetEnumValue(enumValue = facetValueAnnotationContent.fixedEnumValue)
        }
    }

    private fun checkFacetEnumValue(enumValue: String?) {
        val annotationBaseData = facetValueAnnotationContent.base
        val annotation: Annotation = annotationBaseData.annotation
        val facetName = annotationBaseData.facetName

        val facet = requireNotNull(schemaAccess.facetByFacetName(facetName))
        val validEnumerationValues =
            if (facet is EnumFacetSchema) {
                facet.enumerationValues.map { it.name }
            } else {
                emptyList()
            }

        if (!validEnumerationValues.contains(enumValue)) {
            throw BuilderMethodSyntaxException(
                annotationBaseData.methodLocation,
                BuilderErrorCode.WRONG_FACET_ENUM_VALUE,
                annotation.annotationClass.annotationText(),
                facetName.longText(),
                enumValue ?: "<no-value>",
                validEnumerationValues,
            )
        }
    }

    fun checkFacetValueType() {
        val annotationBaseData = facetValueAnnotationContent.base
        val methodLocation = annotationBaseData.methodLocation
        val facetName = annotationBaseData.facetName

        val facetFromSchema = checkKnownFacetAndReturn()
        val classInformation = getTypeClass(facetValueAnnotationContent)
        val typeClass: KClass<*> = classInformation.clazz

        when (facetFromSchema) {
            is TextFacetSchema ->
                if (typeClass != String::class) {
                    throw BuilderMethodSyntaxException(
                        methodLocation,
                        BuilderErrorCode.BUILDER_PARAM_WRONG_TEXT_FACET_TYPE,
                        facetName.longText(),
                        SUPPORTED_COLLECTION_TYPES,
                    )
                }
            is NumberFacetSchema ->
                if (typeClass != Int::class) {
                    throw BuilderMethodSyntaxException(
                        methodLocation,
                        BuilderErrorCode.BUILDER_PARAM_WRONG_NUMBER_FACET_TYPE,
                        facetName.longText(),
                        SUPPORTED_COLLECTION_TYPES,
                    )
                }
            is BooleanFacetSchema ->
                if (typeClass != Boolean::class) {
                    throw BuilderMethodSyntaxException(
                        methodLocation,
                        BuilderErrorCode.BUILDER_PARAM_WRONG_BOOLEAN_FACET_TYPE,
                        facetName.longText(),
                        SUPPORTED_COLLECTION_TYPES,
                    )
                }
            is EnumFacetSchema ->
                if (
                    !isEnumType(facetFromSchema.enumerationType, typeClass) ||
                        !isCompatibleEnum(facetFromSchema.enumerationType, typeClass)
                ) {
                    throw BuilderMethodSyntaxException(
                        methodLocation,
                        BuilderErrorCode.BUILDER_PARAM_WRONG_ENUM_FACET_TYPE,
                        facetName.longText(),
                        SUPPORTED_COLLECTION_TYPES,
                        facetFromSchema.enumerationType.shortText(),
                        facetFromSchema.enumerationValues,
                    )
                }
            is ReferenceFacetSchema ->
                if (typeClass != ConceptIdentifier::class) {
                    throw BuilderMethodSyntaxException(
                        methodLocation,
                        BuilderErrorCode.BUILDER_PARAM_WRONG_REFERENCE_FACET_TYPE,
                        facetName.longText(),
                        SUPPORTED_COLLECTION_TYPES,
                    )
                }
        }

        if (facetValueAnnotationContent.base.ignoreNullValue && !classInformation.isValueNullable) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_IGNORE_NULL_ANNOTATION_WITHOUT_NULLABLE_TYPE,
            )
        }
        if (!facetValueAnnotationContent.base.ignoreNullValue && classInformation.isValueNullable) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_PARAM_NULLABLE_TYPE_WITHOUT_IGNORE_NULL_ANNOTATION,
            )
        }
    }

    private fun getTypeClass(facetValueAnnotationContent: FacetValueAnnotationContent): KTypeClassInformation {
        if (facetValueAnnotationContent.base.typeClass != null) {
            return KTypeUtil.classInformationFromClass(facetValueAnnotationContent.base.typeClass, false)
        }
        val methodLocation = facetValueAnnotationContent.base.methodLocation

        val type = facetValueAnnotationContent.base.type
        if (type != null) {
            val classInformationIncludingCollection =
                checkIsClassParameterType(
                    type,
                    methodLocation,
                    BuilderErrorCode.BUILDER_PARAM_WRONG_SET_FACET_VALUE_PARAMETER,
                )
            val classInformation =
                extractValueClassFromCollectionIfCollection(classInformationIncludingCollection, methodLocation)
            return classInformation
        }

        throw RuntimeException("Neither the 'type' nor the 'type class' were defined for $facetValueAnnotationContent")
    }

    private fun isEnumType(enumerationType: KClass<*>?, actualType: KClass<*>): Boolean {
        return enumerationType != null && actualType.isEnum
    }

    private fun isCompatibleEnum(enumerationType: KClass<*>?, actualType: KClass<*>): Boolean {
        return enumerationType != null &&
            EnumUtil.isSameOrSubsetEnumerationClass(fullEnumClass = enumerationType, fullOrSubsetEnumClass = actualType)
    }
}
