package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.ConceptSchema
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongCardinalitySchemaSyntaxException
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.type.getAnnotation
import org.codeblessing.sourceamazing.schema.type.getAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.schema.type.hasAnnotation
import org.codeblessing.sourceamazing.schema.type.hasAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.schema.type.isEnum
import org.codeblessing.sourceamazing.schema.type.isPrivate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties

object SchemaTraverser {

    @Throws(SyntaxException::class)
    fun createSchemaFromRootDefinitionClass(schemaDefinitionClass: KClass<*>): SchemaImpl {
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
        val conceptName = ConceptName.of(definitionClass)
        val facets = definitionClass.memberProperties
            .filter { it.hasAnnotation(Facet::class) }
            .map { createFacetSchema(conceptName,it) }

        return ConceptSchemaImpl(conceptName, facets)
    }

    private fun createFacetSchema(
        conceptName: ConceptName,
        facetProperty: KProperty1<out Any, *>,
    ): FacetSchema {
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


    private fun createFacetSchema(
        conceptName: ConceptName,
        facetName: FacetName,
        facetClass: KClass<*>,
        conceptNames: List<ConceptName>
    ): FacetSchema {
        val unvalidatedFacetSchema = createUnvalidatedFacetSchema(facetName, facetClass)
        return validatedFacetSchema(unvalidatedFacetSchema, conceptName, conceptNames)
    }

    private class UnvalidatedFacetSchema (
        val facetName: FacetName,
        val facetType: FacetType,
        val minimumOccurrences: Int,
        val maximumOccurrences: Int,
        val enumerationType: KClass<*>?,
        val referencedConceptClasses: List<KClass<*>>,
    )

    private fun createUnvalidatedFacetSchema(
        facetName: FacetName,
        facetClass: KClass<*>,
    ): UnvalidatedFacetSchema {
        return if(facetClass.hasAnnotationIncludingSuperclasses(StringFacet::class)) {
            val stringFacet = facetClass.getAnnotationIncludingSuperclasses(StringFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.TEXT,
                minimumOccurrences = stringFacet.minimumOccurrences,
                maximumOccurrences = stringFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

            )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(BooleanFacet::class)) {
            val booleanFacet = facetClass.getAnnotationIncludingSuperclasses(BooleanFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.BOOLEAN,
                minimumOccurrences = booleanFacet.minimumOccurrences,
                maximumOccurrences = booleanFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(IntFacet::class)) {
            val intFacet = facetClass.getAnnotationIncludingSuperclasses(IntFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.NUMBER,
                minimumOccurrences = intFacet.minimumOccurrences,
                maximumOccurrences = intFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(EnumFacet::class)) {
            val enumFacet = facetClass.getAnnotationIncludingSuperclasses(EnumFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.TEXT_ENUMERATION,
                minimumOccurrences = enumFacet.minimumOccurrences,
                maximumOccurrences = enumFacet.maximumOccurrences,
                enumerationType = enumFacet.enumerationClass,
                referencedConceptClasses = emptyList(),

                )
        } else if(facetClass.hasAnnotationIncludingSuperclasses(ReferenceFacet::class)) {
            val referenceFacet = facetClass.getAnnotationIncludingSuperclasses(ReferenceFacet::class)
            UnvalidatedFacetSchema(
                facetName = facetName,
                facetType = FacetType.REFERENCE,
                minimumOccurrences = referenceFacet.minimumOccurrences,
                maximumOccurrences = referenceFacet.maximumOccurrences,
                enumerationType = null,
                referencedConceptClasses = referenceFacet.referencedConcepts.toList(),

                )
        } else {
            throw IllegalStateException("No supported facet type on $facetClass.")
        }
    }

    private fun validatedFacetSchema(
        facetSchema: UnvalidatedFacetSchema,
        conceptName: ConceptName,
        conceptNames: List<ConceptName>
    ): FacetSchema {
        val facetName: FacetName = facetSchema.facetName

        val facetType = facetSchema.facetType
        val minimumOccurrences = facetSchema.minimumOccurrences
        val maximumOccurrences = facetSchema.maximumOccurrences
        val enumerationType =  facetSchema.enumerationType
        val referencedConcepts =  facetSchema.referencedConceptClasses
            .map { it.toConceptName() }.toSet()


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

        referencedConcepts.forEach { referencedConcept ->
            if(!conceptNames.contains(referencedConcept)) {
                throw WrongTypeSyntaxException(SchemaErrorCode.FACET_UNKNOWN_REFERENCED_CONCEPT, facetName, conceptName, referencedConcept, conceptNames)
            }
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
