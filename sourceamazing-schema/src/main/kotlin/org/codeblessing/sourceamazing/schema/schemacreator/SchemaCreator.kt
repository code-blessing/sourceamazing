package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.*
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongPropertySyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalitySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.type.*
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoExtensionFunctions
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoFunctions
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasNoGenericTypeParameters
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkIsOrdinaryInterface
import org.codeblessing.sourceamazing.schema.type.KTypeUtil.KTypeClassInformation
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

object SchemaCreator {
    val supportedCollectionClasses: List<KClass<*>> = listOf(List::class, Set::class, Collection::class, Iterable::class)
    private const val CONCEPT_CLASS_DESCRIPTION = "Concept Class"
    private const val FACET_CLASS_DESCRIPTION = "Facet Class"

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

        overallConceptValidation(concepts)

        return SchemaImpl(concepts)
    }

    private fun overallConceptValidation(concepts: Map<ConceptName, ConceptSchema>) {
        // TODO validate that all referenced concepts are available?
    }

    private fun createConceptSchema(definitionClass: KClass<*>): ConceptSchema {
        validateConceptClass(definitionClass)
        val conceptName = ConceptName.of(definitionClass)
        val facets = definitionClass.memberProperties
            .filter { it.hasAnnotation(Facet::class) }
            .map { createFacetSchema(it) }
            .onEach { validatedFacetSchema(it, conceptName) }

        return ConceptSchemaImpl(conceptName, facets)
    }

    private fun validateConceptClass(definitionClass: KClass<*>) {
        checkIsOrdinaryInterface(definitionClass, CONCEPT_CLASS_DESCRIPTION)
        checkHasNoGenericTypeParameters(definitionClass, CONCEPT_CLASS_DESCRIPTION)
        checkHasNoExtensionFunctions(definitionClass, CONCEPT_CLASS_DESCRIPTION)
        checkHasNoFunctions(definitionClass, CONCEPT_CLASS_DESCRIPTION)
        // TODO check has no superclass
        RelevantMethodFetcher.ownMemberFunctions(definitionClass).isEmpty()
    }

    private fun createFacetSchema(
        facetProperty: KProperty1<out Any, *>,
    ): FacetSchema {
        validateFacet(facetProperty)
        val facetName = FacetName.of(facetProperty.name)
        val returningType = facetProperty.returnType

        val classifierClass = returningType.classifier as KClass<*>
        val isCollection = classifierClass.isFacetCollection()
        val isNullable = returningType.isMarkedNullable
        val facetClass: KClass<*> = facetClass(returningType, isCollection)
        val facetType: FacetType = facetClassToFacetType(facetClass)
        val minimumOccurrences: Int = if(isNullable || isCollection) 0 else 1
        val maximumOccurrences: Int = if(isCollection) Int.MAX_VALUE else 1

        return FacetSchemaImpl(
            facetName = facetName,
            facetType = facetType,
            minimumOccurrences = minimumOccurrences,
            maximumOccurrences = maximumOccurrences,
            referencingConcepts = if(facetType == FacetType.REFERENCE) referencedConcepts(facetProperty) else emptySet(),
            enumerationType = if(facetType == FacetType.TEXT_ENUMERATION) facetClass else null,
        )
    }

    private fun validateFacet(property: KProperty<*>) {
        val facetDescription = property.name // TODO enhance with more information about the class

        PropertyCheckerUtil.checkHasNoValueParameters(property, facetDescription)
        PropertyCheckerUtil.checkHasNoExtensionReceiverParameter(property, facetDescription)
        PropertyCheckerUtil.checkHasNoTypeParameter(property, facetDescription)
        PropertyCheckerUtil.checkHasNoFunctionBody(property, facetDescription)
        PropertyCheckerUtil.checkHasReturnType(property, facetDescription)

        val returnTypeClassesInformation = classesInformationFromReturnType(property, facetDescription)
        val returnTypeCollectionClassInfo = collectionClassInfo(returnTypeClassesInformation)
        val returnTypeValueClassInfo = valueClassInfo(returnTypeClassesInformation)

        if(returnTypeCollectionClassInfo != null) {
            if(supportedCollectionClasses.none { returnTypeCollectionClassInfo.clazz == it  }) {
                throw WrongPropertySyntaxException(property, SchemaErrorCode.RETURN_TYPE_IS_WRONG_CLASS_ONLY_COLLECTION_OR_CLASS, supportedCollectionClasses, returnTypeCollectionClassInfo.clazz.longText())
            }

            if(returnTypeValueClassInfo.isValueNullable) {
                throw WrongPropertySyntaxException(property, SchemaErrorCode.RETURN_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED)
            }
        }

        if(property.hasAnnotation<References>()) {
            val possibleClassesToReference = property.findAnnotation<References>()?.possibleClassesToReference?.toList() ?: emptyList()

            if(possibleClassesToReference.isEmpty()) {
                throw WrongPropertySyntaxException(property, SchemaErrorCode.NO_CONCEPTS_TO_QUERY)
            }

            if(!isInheritanceCompatibleClass(returnTypeValueClassInfo.clazz, possibleClassesToReference)) {
                throw WrongPropertySyntaxException(property, SchemaErrorCode.RETURN_TYPE_MUST_BE_INHERITABLE, returnTypeValueClassInfo.clazz.longText(), possibleClassesToReference.toList().map { it.longText() })
            }
        }



        // TODO add proper validation of facetProperty for supported annotations
//
//        checkHasExactlyOneOfAnnotation(
//            annotations = listOf(
//                StringFacet::class,
//                BooleanFacet::class,
//                IntFacet::class,
//                EnumFacet::class,
//                ReferenceFacet::class,
//            ),
//            classToInspect = facetProperty,
//            classDescription = FACET_CLASS_DESCRIPTION
//        )
//        checkHasOnlyAnnotations(listOf(
//            StringFacet::class,
//            BooleanFacet::class,
//            IntFacet::class,
//            EnumFacet::class,
//            ReferenceFacet::class,
//        ), facetProperty, FACET_CLASS_DESCRIPTION)
    }

    private fun isInheritanceCompatibleClass(clazz: KClass<*>, classesCompatibleWithClazz: List<KClass<*>>): Boolean {
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


    private fun referencedConcepts(facetProperty: KProperty1<out Any, *>): Set<ConceptName> {
        return facetProperty.getAnnotation<References>().possibleClassesToReference.map { it.toConceptName() }.toSet()
    }

    private fun KClass<*>.isFacetCollection(): Boolean {
        return this == List::class || this == Set::class
    }

    private fun facetClass(returningType: KType, isCollection: Boolean): KClass<*> {
        return if(isCollection) {
            returningType.arguments[0].type!!.classifier as KClass<*>
        } else {
            returningType.classifier as KClass<*>
        }
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
