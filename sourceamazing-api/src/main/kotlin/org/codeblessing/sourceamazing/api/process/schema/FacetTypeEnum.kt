package org.codeblessing.sourceamazing.api.process.schema

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import kotlin.reflect.KClass
import kotlin.reflect.KType

enum class FacetTypeEnum {
    TEXT,
    NUMBER,
    BOOLEAN,
    REFERENCE,
    TEXT_ENUMERATION
    ;

    fun isCompatibleInputType(facetValue: Any): Boolean {
        return matchingEnumByInputFacetTypeClass(facetValue::class)
            ?.let { matchingEnum -> matchingEnum == this }
            ?: false
    }

    companion object {

        fun matchingEnumByInputFacetTypeClass(classType: KClass<*>): FacetTypeEnum? {

            if(classType.java.isEnum) {
                return TEXT_ENUMERATION
            }
            return when(classType) {
                String::class -> TEXT
                Int::class -> NUMBER // TODO is the cast to Int done properly in case of Long values?
                Long::class -> NUMBER
                Boolean::class -> BOOLEAN
                ConceptIdentifier::class -> REFERENCE
                else -> null
            }
        }

        fun matchingEnumByTypeClass(classType: KClass<*>): FacetTypeEnum? {
            if(classType.java.isEnum) {
                return TEXT_ENUMERATION
            }

            return when(classType) {
                String::class -> TEXT
                Int::class -> NUMBER // TODO is the cast to Int done properly in case of Long values?
                Long::class -> NUMBER
                Boolean::class -> BOOLEAN
                else -> if (referencedTypeConceptName(classType) != null) REFERENCE else null
            }
        }

        fun supportedTypes(): String {
            return "${String::class.simpleName},${Int::class.simpleName},${Long::class.simpleName},${Boolean::class.simpleName},${ConceptIdentifier::class.qualifiedName},enum"
        }

        fun referencedTypeConceptName(clazzType: KClass<*>): ConceptName? {
            return clazzType.java.getAnnotation(Concept::class.java)?.let { ConceptName.of(it.conceptName) }
        }
    }
}
