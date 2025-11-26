package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongFacetSchemaException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongPropertySyntaxException
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasMemberExtensionFunctions
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasMemberFunctions
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isOrdinaryInterface
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasExtensionReceiverParameter
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasFunctionBody
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasReturnType
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasTypeParameter
import org.codeblessing.sourceamazing.utils.type.KPropertyUtil.hasValueParameters
import org.codeblessing.sourceamazing.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.utils.type.KTypeUtil.KTypeClassInformation
import org.codeblessing.sourceamazing.utils.type.isEnum
import org.codeblessing.sourceamazing.utils.type.isPrivate

object SchemaCreator {
    val supportedCollectionClasses: List<KClass<*>> =
        listOf(List::class, Set::class, Collection::class, Iterable::class)
    val supportedDataClasses: List<KClass<*>> = listOf(String::class, Boolean::class, Int::class)

    @Throws(SyntaxException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: KClass<*>): SchemaImpl {
        val concepts: MutableMap<ConceptName, ConceptSchema> = mutableMapOf()

        val conceptClassesToProcess: MutableSet<KClass<*>> = mutableSetOf(schemaDefinitionClass)
        while (conceptClassesToProcess.isNotEmpty()) {
            val conceptSchema = createConceptSchema(conceptClassesToProcess.removeFirst())
            concepts.put(conceptSchema.conceptName, conceptSchema)

            conceptSchema.facets
                .flatMap { it.referencingConcepts }
                .filterNot { it in concepts }
                .map { it.clazz }
                .let { conceptClassesToProcess.addAll(it) }
        }

        return SchemaImpl(concepts)
    }

    private fun createConceptSchema(definitionClass: KClass<*>): ConceptSchema {
        validateConceptClass(definitionClass)
        val conceptName = ConceptName.of(definitionClass)

        val facets =
            definitionClass.memberProperties
                .map { createFacetSchema(it, conceptName) }
                .onEach { validatedFacetSchema(it, conceptName) }

        return ConceptSchemaImpl(conceptName, facets)
    }

    private fun validateConceptClass(definitionClass: KClass<*>) {
        if (!isOrdinaryInterface(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE,
                definitionClass,
            )
        }
        if (hasGenericTypeParameters(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER,
                definitionClass.longText(),
                definitionClass.typeParameters,
            )
        }

        if (hasMemberExtensionFunctions(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS,
                definitionClass,
                definitionClass.memberExtensionFunctions,
            )
        }
        if (hasMemberFunctions(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS,
                definitionClass,
                definitionClass.memberFunctions,
            )
        }

        val extensionProperty = definitionClass.memberExtensionProperties.firstOrNull()
        if (extensionProperty != null) {
            throw WrongPropertySyntaxException(
                extensionProperty,
                SchemaErrorCode.PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE,
            )
        }
    }

    private fun createFacetSchema(
        facetProperty: KProperty1<out Any, *>,
        conceptName: ConceptName,
    ): FacetSchema {
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

        val returnTypeClassesInformation =
            classesInformationFromReturnType(facetProperty, facetDescription)
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
            !returnTypeValueClassInfo.clazz.isEnum &&
                supportedDataClasses.none { returnTypeValueClassInfo.clazz == it }

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
                    facetProperty.findAnnotation<References>()?.possibleClassesToReference?.toSet()
                        ?: emptySet()
                if (possibleClassesToReference.isEmpty()) {
                    throw WrongPropertySyntaxException(
                        facetProperty,
                        SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST,
                        facetName,
                        conceptName,
                    )
                }

                if (
                    !isInheritanceCompatibleClass(
                        returnTypeValueClassInfo.clazz,
                        possibleClassesToReference,
                    )
                ) {
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

            validateConceptClass(returnTypeValueClassInfo.clazz)
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

        return FacetSchemaImpl(
            conceptName = conceptName,
            facetName = facetName,
            facetType = facetType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
            referencingConcepts =
                if (facetType == FacetType.REFERENCE)
                    referencedClasses.map { it.toConceptName() }.toSet()
                else emptySet(),
            enumerationType = if (facetType == FacetType.TEXT_ENUMERATION) facetClass else null,
        )
    }

    private fun isInheritanceCompatibleClass(
        clazz: KClass<*>,
        classesCompatibleWithClazz: Set<KClass<*>>,
    ): Boolean {
        return classesCompatibleWithClazz.all { classCompatibleWithClazz ->
            classCompatibleWithClazz == clazz || classCompatibleWithClazz.isSubclassOf(clazz)
        }
    }

    private fun collectionClassInfo(
        classesInformation: List<KTypeClassInformation>
    ): KTypeClassInformation? {
        return if (hasCollection(classesInformation)) classesInformation.first() else null
    }

    private fun valueClassInfo(
        classesInformation: List<KTypeClassInformation>
    ): KTypeClassInformation {
        return if (hasCollection(classesInformation)) classesInformation.last()
        else classesInformation.first()
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

    private fun <T> MutableSet<T>.removeFirst(): T {
        val first = this.first()
        this.remove(first)
        return first
    }

    private fun validatedFacetSchema(
        facetSchema: FacetSchema,
        conceptName: ConceptName,
    ): FacetSchema {
        val facetName: FacetName = facetSchema.facetName

        val facetType = facetSchema.facetType
        val minimumOccurrences = facetSchema.minimumOccurrences
        val maximumOccurrences = facetSchema.maximumOccurrences
        val enumerationType = facetSchema.enumerationType
        val referencedConcepts = facetSchema.referencingConcepts

        if (facetType == FacetType.TEXT_ENUMERATION) {
            if (enumerationType == null || !enumerationType.isEnum) {
                throw WrongFacetSchemaException(
                    SchemaErrorCode.FACET_ENUM_INVALID,
                    facetName,
                    conceptName,
                    enumerationType ?: "null",
                )
            }

            if (enumerationType.isPrivate) {
                throw WrongFacetSchemaException(
                    SchemaErrorCode.FACET_ENUM_HAS_PRIVATE_MODIFIER,
                    facetName,
                    conceptName,
                    enumerationType,
                )
            }
        }

        if (facetType == FacetType.REFERENCE && referencedConcepts.isEmpty()) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST,
                facetName,
                conceptName,
            )
        }

        if (facetType != FacetType.REFERENCE && referencedConcepts.isNotEmpty()) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.FACET_NOT_REFERENCE_NOT_EMPTY_CONCEPT_LIST,
                facetName,
                conceptName,
                facetType,
                referencedConcepts,
            )
        }

        if (minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.NO_NEGATIVE_FACET_CARDINALITIES,
                facetName,
                conceptName,
                minimumOccurrences,
                maximumOccurrences,
            )
        }

        if (minimumOccurrences > maximumOccurrences) {
            throw WrongFacetSchemaException(
                SchemaErrorCode.WRONG_FACET_CARDINALITIES,
                facetName,
                conceptName,
                minimumOccurrences,
                maximumOccurrences,
            )
        }

        return FacetSchemaImpl(
            conceptName = conceptName,
            facetName = facetName,
            facetType = facetType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
            referencingConcepts = referencedConcepts,
            enumerationType = enumerationType,
        )
    }

    private fun KClass<*>.longText(): String {
        return java.name
    }
}
