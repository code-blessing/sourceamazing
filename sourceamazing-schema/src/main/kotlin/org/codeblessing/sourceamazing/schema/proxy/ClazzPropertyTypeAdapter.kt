package org.codeblessing.sourceamazing.schema.proxy

import kotlin.reflect.KType
import org.codeblessing.sourceamazing.schema.clazzgraph.ClazzInstance
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil

object ClazzPropertyTypeAdapter {

    fun adaptClazzPropertyValue(classProperty: ClassProperty, clazzInstance: ClazzInstance, type: KType): Any? {
        val clazzPropertyValues =
            requireNotNull(clazzInstance.clazzPropertyValues[classProperty]) {
                "Clazz property values not found for property $classProperty and clazz ${clazzInstance.clazz}."
            }

        val clazzPropertyValueList = clazzPropertyValues.map(::mapClazzPropertyValue)
        return adaptResultToType(type, clazzPropertyValueList)
    }

    private fun mapClazzPropertyValue(clazzPropertyValue: Any): Any {
        return if (clazzPropertyValue is ClazzInstance) {
            ClazzInstanceFactory.createInstanceOrProxy(
                clazz = clazzPropertyValue.clazz.clazz,
                clazzInstance = clazzPropertyValue,
            )
        } else {
            clazzPropertyValue
        }
    }

    private fun adaptResultToType(type: KType, resultList: List<Any>): Any? {
        val returnTypeClassesInformation = KTypeUtil.classesInformationFromKType(type)
        val returnTypeCollectionClassInfo = collectionClassInfo(returnTypeClassesInformation)
        val returnTypeValueClassInfo = valueClassInfo(returnTypeClassesInformation)

        return if (returnTypeCollectionClassInfo != null) {
            when (returnTypeCollectionClassInfo.clazz) {
                List::class -> resultList.toList()
                Set::class -> resultList.toSet()
                Collection::class -> resultList.toList()
                Iterable::class -> resultList.toList()
                else ->
                    throw IllegalStateException(
                        "Collection return type ${returnTypeCollectionClassInfo.clazz} not supported."
                    )
            }
        } else {
            if (resultList.isEmpty()) {
                if (returnTypeValueClassInfo.isValueNullable) {
                    null
                } else {
                    throw IllegalStateException("No value was provided but the return type is not nullable.")
                }
            } else {
                resultList.first()
            }
        }
    }

    fun collectionClassInfo(
        classesInformation: List<KTypeUtil.KTypeClassInformation>
    ): KTypeUtil.KTypeClassInformation? {
        return if (hasCollection(classesInformation)) classesInformation.first() else null
    }

    fun valueClassInfo(classesInformation: List<KTypeUtil.KTypeClassInformation>): KTypeUtil.KTypeClassInformation {
        return if (hasCollection(classesInformation)) classesInformation.last() else classesInformation.first()
    }

    private fun hasCollection(classesInformation: List<KTypeUtil.KTypeClassInformation>): Boolean {
        return classesInformation.size == 2
    }
}
