package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.*
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongPropertySyntaxException
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isOrdinaryInterface
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasExtensionReceiverParameter
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasFunctionBody
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasReturnType
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasTypeParameter
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasValueParameters
import org.codeblessing.sourceamazing.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.utils.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.utils.type.isEnum

object FacetSchemaCreator {
    val supportedCollectionClasses: List<KClass<*>> =
        listOf(List::class, Set::class, Collection::class, Iterable::class)
    val supportedDataClasses: List<KClass<*>> = listOf(String::class, Boolean::class, Int::class)

    fun createFacetSchema(facetProperty: KProperty<*>, conceptName: ConceptName): FacetSchema {
        checkPropertyHasNoValueParameters(facetProperty)
        checkPropertyHasNoExtentionReceiverParameter(facetProperty)
        checkPropertyHasNoTypeParameter(facetProperty)
        checkPropertyHasNoFunctionBody(facetProperty)
        checkPropertyHasReturnType(facetProperty)

        val returnTypeClasses: List<KTypeClassInformation> =
            checkNoInvalidReturnType(facetProperty) { KTypeUtil.classesInformationFromKType(facetProperty.returnType) }
        checkReturnClassIsCollectionOfOrSingleClass(facetProperty, returnTypeClasses)

        val returnTypeCollectionClass = collectionClassInfo(returnTypeClasses)
        val returnTypeValueClass = valueClassInfo(returnTypeClasses)

        if (returnTypeCollectionClass != null) {
            checkCollectionClassIsSupportedClass(facetProperty, returnTypeCollectionClass)
            checkValueClassNotNullableIfCollectionClassAvailable(facetProperty, returnTypeValueClass)
        }

        val isReference: Boolean = isReferenceType(returnTypeValueClass)
        if (!isReference) {
            checkHasNoReferencesAnnotation(facetProperty)
        } else {
            checkReferenceClassMustBeInterface(facetProperty, returnTypeValueClass)
            checkReferenceClassIsValidConcept(returnTypeValueClass)
            checkPossibleReferencesAnnotationNotEmptyIfAnnotationAvailable(facetProperty, conceptName)
            checkPossibleReferencesHaveNoDuplicates(facetProperty, conceptName)
            checkPossibleReferencesIsInheritanceCompatibleClass(facetProperty, returnTypeValueClass)
        }

        return createFacetSchemaImplementation(
            conceptName,
            facetProperty,
            returnTypeCollectionClass,
            returnTypeValueClass,
        )
    }

    private fun checkPropertyHasNoValueParameters(facetProperty: KProperty<*>) {
        if (hasValueParameters(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_CAN_NOT_HAVE_VALUE_PARAMS,
                facetProperty.toDescription(),
            )
        }
    }

    private fun checkPropertyHasNoExtentionReceiverParameter(facetProperty: KProperty<*>) {
        if (hasExtensionReceiverParameter(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_HAS_RECEIVER_PARAM,
                facetProperty.toDescription(),
            )
        }
    }

    private fun checkPropertyHasNoTypeParameter(facetProperty: KProperty<*>) {
        if (hasTypeParameter(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS,
                facetProperty.toDescription(),
                facetProperty.typeParameters,
            )
        }
    }

    private fun checkPropertyHasNoFunctionBody(facetProperty: KProperty<*>) {
        if (hasFunctionBody(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT,
                facetProperty.toDescription(),
            )
        }
    }

    private fun checkPropertyHasReturnType(facetProperty: KProperty<*>) {
        if (!hasReturnType(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.PROPERTY_MUST_HAVE_RETURN_TYPE,
                facetProperty.toDescription(),
            )
        }
    }

    private fun <T> checkNoInvalidReturnType(property: KProperty<*>, block: () -> T): T {
        return try {
            block()
        } catch (ex: IllegalStateException) {
            throw WrongPropertySyntaxException(
                property,
                SchemaErrorCode.RETURN_TYPE_IS_INVALID,
                property.toDescription(),
                ex.message ?: "",
            )
        }
    }

    private fun checkReturnClassIsCollectionOfOrSingleClass(
        property: KProperty<*>,
        returnTypeClassesInformation: List<KTypeClassInformation>,
    ) {
        if (returnTypeClassesInformation.size > 2 || returnTypeClassesInformation.isEmpty()) {
            throw WrongPropertySyntaxException(
                property,
                SchemaErrorCode.RETURN_TYPE_IS_INVALID_ONLY_COLLECTION_OR_CLASS,
                property.toDescription(),
                supportedCollectionClasses,
            )
        }
    }

    private fun checkCollectionClassIsSupportedClass(
        facetProperty: KProperty<*>,
        returnTypeCollectionClass: KTypeClassInformation?,
    ) {
        if (returnTypeCollectionClass != null) {
            if (supportedCollectionClasses.none { returnTypeCollectionClass.clazz == it }) {
                throw WrongPropertySyntaxException(
                    facetProperty,
                    SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS,
                    supportedCollectionClasses,
                    returnTypeCollectionClass.clazz.longText(),
                )
            }
        }
    }

    private fun checkValueClassNotNullableIfCollectionClassAvailable(
        facetProperty: KProperty<*>,
        returnTypeValueClass: KTypeClassInformation,
    ) {
        if (returnTypeValueClass.isValueNullable) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED,
            )
        }
    }

    private fun checkHasNoReferencesAnnotation(facetProperty: KProperty<*>) {
        if (facetProperty.hasAnnotation<References>()) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.REFERENCE_ANNOTATION_ONLY_FOR_REFERENCE_TYPES,
            )
        }
    }

    private fun checkReferenceClassMustBeInterface(
        facetProperty: KProperty<*>,
        returnTypeValueClass: KTypeClassInformation,
    ) {
        if (!isOrdinaryInterface(returnTypeValueClass.clazz)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE,
                returnTypeValueClass.clazz.longText(),
            )
        }
    }

    private fun checkReferenceClassIsValidConcept(returnTypeValueClass: KTypeClassInformation) {
        ConceptClassValidator.validateConceptClass(returnTypeValueClass.clazz)
    }

    private fun checkPossibleReferencesAnnotationNotEmptyIfAnnotationAvailable(
        facetProperty: KProperty<*>,
        conceptName: ConceptName,
    ) {
        if (facetProperty.hasAnnotation<References>()) {
            val possibleClassesToReference = facetProperty.possibleReferencesClasses()
            if (possibleClassesToReference.isEmpty()) {
                throw WrongPropertySyntaxException(
                    facetProperty,
                    SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST,
                    facetProperty.toFacetName(),
                    conceptName,
                )
            }
        }
    }

    private fun checkPossibleReferencesHaveNoDuplicates(facetProperty: KProperty<*>, conceptName: ConceptName) {
        if (facetProperty.hasAnnotation<References>()) {
            val possibleClassesToReference = facetProperty.possibleReferencesClasses()
            if (possibleClassesToReference.size != possibleClassesToReference.toSet().size) {
                throw WrongPropertySyntaxException(
                    facetProperty,
                    SchemaErrorCode.REFERENCE_ANNOTATION_CONTAINS_DUPLICATES,
                    facetProperty.toFacetName(),
                    conceptName,
                )
            }
        }
    }

    private fun checkPossibleReferencesIsInheritanceCompatibleClass(
        facetProperty: KProperty<*>,
        returnTypeValueClass: KTypeClassInformation,
    ) {
        if (facetProperty.hasAnnotation<References>()) {
            val possibleClassesToReference = facetProperty.possibleReferencesClasses().toSet()

            if (!isInheritanceCompatibleClass(returnTypeValueClass.clazz, possibleClassesToReference)) {
                throw WrongPropertySyntaxException(
                    facetProperty,
                    SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE,
                    returnTypeValueClass.clazz.longText(),
                    possibleClassesToReference.toList().map { it.longText() },
                )
            }
        }
    }

    private fun getReferenceableConcepts(
        facetProperty: KProperty<*>,
        returnTypeValueClass: KTypeClassInformation,
    ): Set<ConceptName> {
        if (!isReferenceType(returnTypeValueClass)) {
            return emptySet()
        }
        return if (facetProperty.hasAnnotation<References>()) {
                facetProperty.possibleReferencesClasses()
            } else {
                setOf(returnTypeValueClass.clazz)
            }
            .map { it.toConceptName() }
            .toSet()
    }

    private fun isReferenceType(returnTypeValueClass: KTypeClassInformation): Boolean {
        return !returnTypeValueClass.clazz.isEnum && supportedDataClasses.none { returnTypeValueClass.clazz == it }
    }

    private fun collectionClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation? {
        return if (hasCollection(classesInformation)) classesInformation.first() else null
    }

    private fun valueClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation {
        return if (hasCollection(classesInformation)) classesInformation.last() else classesInformation.first()
    }

    private fun hasCollection(classesInformation: List<KTypeClassInformation>): Boolean {
        return classesInformation.size == 2
    }

    private fun isInheritanceCompatibleClass(clazz: KClass<*>, classesCompatibleWithClazz: Set<KClass<*>>): Boolean {
        return classesCompatibleWithClazz.all { classCompatibleWithClazz ->
            classCompatibleWithClazz == clazz || classCompatibleWithClazz.isSubclassOf(clazz)
        }
    }

    private fun createFacetSchemaImplementation(
        conceptName: ConceptName,
        facetProperty: KProperty<*>,
        returnTypeCollectionClass: KTypeClassInformation?,
        returnTypeValueClass: KTypeClassInformation,
    ): FacetSchema {
        val referencingConcepts: Set<ConceptName> = getReferenceableConcepts(facetProperty, returnTypeValueClass)

        val facetName = facetProperty.name.toFacetName()
        val isCollection = returnTypeCollectionClass != null
        val isNullable = facetProperty.returnType.isMarkedNullable
        val facetType: FacetType = facetClassToFacetType(returnTypeValueClass)
        val minimumOccurrences: Int = if (isNullable || isCollection) 0 else 1
        val maximumOccurrences: Int = if (isCollection) Int.MAX_VALUE else 1

        return when (facetType) {
            FacetType.TEXT ->
                TextFacetSchemaImpl(
                    conceptName = conceptName,
                    facetName = facetName,
                    facetType = facetType,
                    minimumOccurrences = minimumOccurrences,
                    maximumOccurrences = maximumOccurrences,
                )
            FacetType.NUMBER ->
                NumberFacetSchemaImpl(
                    conceptName = conceptName,
                    facetName = facetName,
                    facetType = facetType,
                    minimumOccurrences = minimumOccurrences,
                    maximumOccurrences = maximumOccurrences,
                )
            FacetType.BOOLEAN ->
                BooleanFacetSchemaImpl(
                    conceptName = conceptName,
                    facetName = facetName,
                    facetType = facetType,
                    minimumOccurrences = minimumOccurrences,
                    maximumOccurrences = maximumOccurrences,
                )
            FacetType.REFERENCE ->
                ReferenceFacetSchemaImpl(
                    conceptName = conceptName,
                    facetName = facetName,
                    facetType = facetType,
                    minimumOccurrences = minimumOccurrences,
                    maximumOccurrences = maximumOccurrences,
                    referencingConcepts = referencingConcepts,
                )
            FacetType.TEXT_ENUMERATION ->
                EnumFacetSchemaImpl(
                    conceptName = conceptName,
                    facetName = facetName,
                    facetType = facetType,
                    minimumOccurrences = minimumOccurrences,
                    maximumOccurrences = maximumOccurrences,
                    enumerationType = returnTypeValueClass.clazz,
                )
        }
    }

    private fun facetClassToFacetType(classInformation: KTypeClassInformation): FacetType {
        return when {
            classInformation.clazz == String::class -> FacetType.TEXT
            classInformation.clazz == Boolean::class -> FacetType.BOOLEAN
            classInformation.clazz == Int::class -> FacetType.NUMBER
            classInformation.clazz.isEnum -> FacetType.TEXT_ENUMERATION
            else -> FacetType.REFERENCE
        }
    }

    private fun KClass<*>.longText(): String {
        return java.name
    }

    private fun KProperty<*>.toDescription(): String = this.toString()

    private fun KProperty<*>.toFacetName(): FacetName = this.name.toFacetName()

    private fun KProperty<*>.possibleReferencesClasses(): List<KClass<*>> {
        return findAnnotation<References>()?.possibleClassesToReference?.toList() ?: emptyList()
    }
}
