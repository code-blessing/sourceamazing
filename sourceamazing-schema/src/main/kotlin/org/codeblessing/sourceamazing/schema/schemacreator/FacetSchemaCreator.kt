package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
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

    fun createFacetSchema(facetProperty: KProperty1<out Any, *>, conceptName: ConceptName): FacetSchema {
        val facetName = FacetName.of(facetProperty.name)
        val facetDescription = facetProperty.toString()
        if (hasValueParameters(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_CAN_NOT_HAVE_VALUE_PARAMS,
                facetDescription,
            )
        }
        if (hasExtensionReceiverParameter(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_HAS_RECEIVER_PARAM,
                facetDescription,
            )
        }
        if (hasTypeParameter(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_HAVE_TYPE_PARAMS,
                facetDescription,
                facetProperty.typeParameters,
            )
        }
        if (hasFunctionBody(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.FUNCTION_MUST_BE_ABSTRACT,
                facetDescription,
            )
        }
        if (!hasReturnType(facetProperty)) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.PROPERTY_MUST_HAVE_RETURN_TYPE,
                facetDescription,
            )
        }

        val returnTypeClassesInformation = classesInformationFromReturnType(facetProperty, facetDescription)
        val returnTypeCollectionClassInfo = collectionClassInfo(returnTypeClassesInformation)
        val returnTypeValueClassInfo = valueClassInfo(returnTypeClassesInformation)

        if (returnTypeCollectionClassInfo != null) {
            if (supportedCollectionClasses.none { returnTypeCollectionClassInfo.clazz == it }) {
                throw WrongPropertySyntaxException(
                    facetProperty,
                    SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS,
                    supportedCollectionClasses,
                    returnTypeCollectionClassInfo.clazz.longText(),
                )
            }

            if (returnTypeValueClassInfo.isValueNullable) {
                throw WrongPropertySyntaxException(
                    facetProperty,
                    SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED,
                )
            }
        }

        val isReference =
            !returnTypeValueClassInfo.clazz.isEnum && supportedDataClasses.none { returnTypeValueClassInfo.clazz == it }

        if (!isReference && facetProperty.hasAnnotation<References>()) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.REFERENCE_ANNOTATION_ONLY_FOR_REFERENCE_TYPES,
            )
        }

        val referencedClasses: Set<KClass<*>>
        if (isReference) {
            val possibleClassesToReference: Set<KClass<*>>
            if (facetProperty.hasAnnotation<References>()) {
                possibleClassesToReference =
                    facetProperty.findAnnotation<References>()?.possibleClassesToReference?.toSet() ?: emptySet()
                if (possibleClassesToReference.isEmpty()) {
                    throw WrongPropertySyntaxException(
                        facetProperty,
                        SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST,
                        facetName,
                        conceptName,
                    )
                }

                if (!isInheritanceCompatibleClass(returnTypeValueClassInfo.clazz, possibleClassesToReference)) {
                    throw WrongPropertySyntaxException(
                        facetProperty,
                        SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE,
                        returnTypeValueClassInfo.clazz.longText(),
                        possibleClassesToReference.toList().map { it.longText() },
                    )
                }
                referencedClasses = possibleClassesToReference
            } else {
                referencedClasses = setOf(returnTypeValueClassInfo.clazz)
            }

            if (!isOrdinaryInterface(returnTypeValueClassInfo.clazz)) {
                throw WrongPropertySyntaxException(
                    facetProperty,
                    SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE,
                    returnTypeValueClassInfo.clazz.longText(),
                )
            }

            ConceptClassValidator.validateConceptClass(returnTypeValueClassInfo.clazz)
        } else {
            referencedClasses = emptySet()
        }

        val returningType = facetProperty.returnType

        val isCollection = returnTypeCollectionClassInfo != null
        val isNullable = returningType.isMarkedNullable
        val facetClass: KClass<*> = returnTypeValueClassInfo.clazz
        val facetType: FacetType = facetClassToFacetType(facetClass)
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
                    referencingConcepts = referencedClasses.map { it.toConceptName() }.toSet(),
                )
            FacetType.TEXT_ENUMERATION ->
                EnumFacetSchemaImpl(
                    conceptName = conceptName,
                    facetName = facetName,
                    facetType = facetType,
                    minimumOccurrences = minimumOccurrences,
                    maximumOccurrences = maximumOccurrences,
                    enumerationType = facetClass,
                )
        }
    }

    private fun isInheritanceCompatibleClass(clazz: KClass<*>, classesCompatibleWithClazz: Set<KClass<*>>): Boolean {
        return classesCompatibleWithClazz.all { classCompatibleWithClazz ->
            classCompatibleWithClazz == clazz || classCompatibleWithClazz.isSubclassOf(clazz)
        }
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

    private fun classesInformationFromReturnType(
        property: KProperty<*>,
        definitionClass: String,
    ): List<KTypeClassInformation> {
        val classesInformation =
            try {
                KTypeUtil.classesInformationFromKType(property.returnType)
            } catch (ex: IllegalStateException) {
                throw WrongPropertySyntaxException(
                    property,
                    SchemaErrorCode.RETURN_TYPE_IS_INVALID,
                    definitionClass,
                    ex.message ?: "",
                )
            }

        if (classesInformation.size > 2 || classesInformation.isEmpty()) {
            throw WrongPropertySyntaxException(
                property,
                SchemaErrorCode.RETURN_TYPE_IS_INVALID_ONLY_COLLECTION_OR_CLASS,
                definitionClass,
                supportedCollectionClasses,
            )
        }
        return classesInformation
    }

    private fun facetClassToFacetType(facetClass: KClass<*>): FacetType {
        return if (facetClass == String::class) {
            FacetType.TEXT
        } else if (facetClass == Boolean::class) {
            FacetType.BOOLEAN
        } else if (facetClass == Int::class) {
            FacetType.NUMBER
        } else if (facetClass.isEnum) {
            FacetType.TEXT_ENUMERATION
        } else {
            FacetType.REFERENCE
        }
    }

    private fun KClass<*>.longText(): String {
        return java.name
    }
}
