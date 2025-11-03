package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.*
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.ConceptSchema
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.FacetSchema
import org.codeblessing.sourceamazing.schema.api.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.exceptions.*
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalitySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.type.*
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.hasMemberExtensionFunctions
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.hasMemberFunctions
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.isOrdinaryInterface
import org.codeblessing.sourceamazing.schema.type.KTypeUtil.KTypeClassInformation
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

object SchemaCreator {
    val supportedCollectionClasses: List<KClass<*>> = listOf(List::class, Set::class, Collection::class, Iterable::class)
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

        val facets = definitionClass.memberProperties
            .map { createFacetSchema(it, conceptName) }
            .onEach { validatedFacetSchema(it, conceptName) }


        return ConceptSchemaImpl(conceptName, facets)
    }

    private fun validateConceptClass(definitionClass: KClass<*>) {
        if(!isOrdinaryInterface(definitionClass)) {
            throw NotInterfaceSyntaxException(definitionClass, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE, definitionClass)
        }
        if(hasGenericTypeParameters(definitionClass)) {
            throw WrongTypeSyntaxException(SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER, definitionClass.longText(), definitionClass.typeParameters)
        }

        if(hasMemberExtensionFunctions(definitionClass)) {
            throw WrongClassStructureSyntaxException(definitionClass, SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS, definitionClass, definitionClass.memberExtensionFunctions)
        }
        if(hasMemberFunctions(definitionClass)) {
            throw WrongClassStructureSyntaxException(definitionClass, SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS, definitionClass, definitionClass.memberFunctions)
        }

        val extensionProperty = definitionClass.memberExtensionProperties.firstOrNull()
        if(extensionProperty != null) {
            throw WrongPropertySyntaxException(extensionProperty, SchemaErrorCode.PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE)
        }

    }

    private fun createFacetSchema(
        facetProperty: KProperty1<out Any, *>,
        conceptName: ConceptName
    ): FacetSchema {
        val facetName = FacetName.of(facetProperty.name)
        val facetDescription = facetProperty.toString()
        PropertyCheckerUtil.checkHasNoValueParameters(facetProperty, facetDescription)
        PropertyCheckerUtil.checkHasNoExtensionReceiverParameter(facetProperty, facetDescription)
        PropertyCheckerUtil.checkHasNoTypeParameter(facetProperty, facetDescription)
        PropertyCheckerUtil.checkHasNoFunctionBody(facetProperty, facetDescription)
        PropertyCheckerUtil.checkHasReturnType(facetProperty, facetDescription)

        val returnTypeClassesInformation = classesInformationFromReturnType(facetProperty, facetDescription)
        val returnTypeCollectionClassInfo = collectionClassInfo(returnTypeClassesInformation)
        val returnTypeValueClassInfo = valueClassInfo(returnTypeClassesInformation)

        if(returnTypeCollectionClassInfo != null) {
            if(supportedCollectionClasses.none { returnTypeCollectionClassInfo.clazz == it  }) {
                throw WrongPropertySyntaxException(facetProperty, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS, supportedCollectionClasses, returnTypeCollectionClassInfo.clazz.longText())
            }

            if(returnTypeValueClassInfo.isValueNullable) {
                throw WrongPropertySyntaxException(facetProperty, SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED)
            }
        }

        val isReference = !returnTypeValueClassInfo.clazz.isEnum && supportedDataClasses.none { returnTypeValueClassInfo.clazz == it }

        if(!isReference && facetProperty.hasAnnotation<References>()) {
            throw WrongPropertySyntaxException(
                facetProperty,
                SchemaErrorCode.REFERENCE_ANNOTATION_ONLY_FOR_REFERENCE_TYPES,
            )
        }

        val referencedClasses: Set<KClass<*>>
        if(isReference) {
            val possibleClassesToReference: Set<KClass<*>>
            if(facetProperty.hasAnnotation<References>()) {
                possibleClassesToReference = facetProperty.findAnnotation<References>()?.possibleClassesToReference?.toSet() ?: emptySet()
                if(possibleClassesToReference.isEmpty()) {
                    throw WrongTypeSyntaxException(SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST, facetName, conceptName)
                }

                if(!isInheritanceCompatibleClass(returnTypeValueClassInfo.clazz, possibleClassesToReference)) {
                    throw WrongPropertySyntaxException(facetProperty, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE, returnTypeValueClassInfo.clazz.longText(), possibleClassesToReference.toList().map { it.longText() })
                }
                referencedClasses = possibleClassesToReference
            } else {
                referencedClasses = setOf(returnTypeValueClassInfo.clazz)
            }

            if(!isOrdinaryInterface(returnTypeValueClassInfo.clazz)) {
                throw WrongPropertySyntaxException(facetProperty, SchemaErrorCode.PROPERTY_RETURN_TYPE_MUST_BE_INTERFACE, returnTypeValueClassInfo.clazz.longText())
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
        val minimumOccurrences: Int = if(isNullable || isCollection) 0 else 1
        val maximumOccurrences: Int = if(isCollection) Int.MAX_VALUE else 1

        return FacetSchemaImpl(
            facetName = facetName,
            facetType = facetType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
            referencingConcepts = if(facetType == FacetType.REFERENCE) referencedClasses.map { it.toConceptName() }.toSet() else emptySet(),
            enumerationType = if(facetType == FacetType.TEXT_ENUMERATION) facetClass else null,
        )
    }

    private fun isInheritanceCompatibleClass(clazz: KClass<*>, classesCompatibleWithClazz: Set<KClass<*>>): Boolean {
        return classesCompatibleWithClazz.all { classCompatibleWithClazz ->
            classCompatibleWithClazz == clazz || classCompatibleWithClazz.isSubclassOf(clazz)
        }
    }

    private fun collectionClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation? {
        return if(hasCollection(classesInformation)) classesInformation.first() else null
    }

    private fun valueClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation {
        return if(hasCollection(classesInformation)) classesInformation.last() else classesInformation.first()
    }

    private fun hasCollection(classesInformation: List<KTypeClassInformation>): Boolean {
        return classesInformation.size == 2
    }

    private fun classesInformationFromReturnType(property: KProperty<*>, definitionClass: String): List<KTypeClassInformation> {
        val classesInformation = try {
            KTypeUtil.classesInformationFromKType(property.returnType)
        } catch (ex: IllegalStateException) {
            throw WrongPropertySyntaxException(property, SchemaErrorCode.RETURN_TYPE_IS_INVALID, definitionClass, ex.message ?: "")
        }

        if (classesInformation.size > 2 || classesInformation.isEmpty()) {
            throw WrongPropertySyntaxException(property, SchemaErrorCode.RETURN_TYPE_IS_INVALID_ONLY_COLLECTION_OR_CLASS, definitionClass, supportedCollectionClasses)
        }
        return classesInformation
    }


    private fun facetClassToFacetType(facetClass: KClass<*>): FacetType {
        return if(facetClass == String::class) {
            FacetType.TEXT
        } else if(facetClass == Boolean::class) {
            FacetType.BOOLEAN
        } else if(facetClass == Int::class) {
            FacetType.NUMBER
        } else if(facetClass.isEnum) {
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
        val enumerationType =  facetSchema.enumerationType
        val referencedConcepts =  facetSchema.referencingConcepts


        if(facetType == FacetType.TEXT_ENUMERATION) {
            if(enumerationType == null || !enumerationType.isEnum) {
                throw WrongTypeSyntaxException(SchemaErrorCode.FACET_ENUM_INVALID, facetName, conceptName, enumerationType ?: "null")
            }

            if(enumerationType.isPrivate) {
                throw WrongTypeSyntaxException(SchemaErrorCode.FACET_ENUM_HAS_PRIVATE_MODIFIER, facetName, conceptName, enumerationType)
            }
        }

        if(facetType == FacetType.REFERENCE && referencedConcepts.isEmpty()) {
            throw WrongTypeSyntaxException(SchemaErrorCode.FACET_REFERENCE_EMPTY_CONCEPT_LIST, facetName, conceptName)
        }

        if(facetType != FacetType.REFERENCE && referencedConcepts.isNotEmpty()) {
            throw WrongTypeSyntaxException(SchemaErrorCode.FACET_NOT_REFERENCE_NOT_EMPTY_CONCEPT_LIST, facetName, conceptName, facetType, referencedConcepts)
        }

        if(minimumOccurrences < 0 || maximumOccurrences < 0) {
            throw WrongCardinalitySchemaSyntaxException(SchemaErrorCode.NO_NEGATIVE_FACET_CARDINALITIES, facetName, conceptName, minimumOccurrences, maximumOccurrences)
        }

        if(minimumOccurrences > maximumOccurrences) {
            throw WrongCardinalitySchemaSyntaxException(SchemaErrorCode.WRONG_FACET_CARDINALITIES, facetName, conceptName, minimumOccurrences, maximumOccurrences)
        }

        return FacetSchemaImpl(
            facetName = facetName,
            facetType = facetType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
            referencingConcepts = referencedConcepts,
            enumerationType = enumerationType,
        )
    }
}
