package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.type.KTypeUtil.KTypeClassInformation
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object QueryMethodUtil {
    val supportedCollectionClasses: List<KClass<*>> = listOf(List::class, Set::class, Collection::class, Iterable::class)

    fun adaptResultToFunctionReturnType(kFunction: KFunction<*>, resultList: List<Any>): Any? {
        val returnTypeClassesInformation = KTypeUtil.classesInformationFromKType(kFunction.returnType)
        val returnTypeCollectionClassInfo = collectionClassInfo(returnTypeClassesInformation)
        val returnTypeValueClassInfo = valueClassInfo(returnTypeClassesInformation)

        return if(returnTypeCollectionClassInfo != null) {
            when (returnTypeCollectionClassInfo.clazz) {
                List::class -> resultList.toList()
                Set::class -> resultList.toSet()
                Collection::class -> resultList.toList()
                Iterable::class -> resultList.toList()
                else -> throw IllegalStateException("Collection return type ${returnTypeCollectionClassInfo.clazz} not supported.")
            }
        } else {
            if(resultList.isEmpty()) {
                if(returnTypeValueClassInfo.isValueNullable) {
                    null
                } else {
                    throw IllegalStateException("No value was provided but the return type is not nullable.")
                }
            } else {
                resultList.first()
            }
        }
    }


    fun collectionClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation? {
        return if(hasCollection(classesInformation)) classesInformation.first() else null
    }

    fun valueClassInfo(classesInformation: List<KTypeClassInformation>): KTypeClassInformation {
        return if(hasCollection(classesInformation)) classesInformation.last() else classesInformation.first()
    }

    private fun hasCollection(classesInformation: List<KTypeClassInformation>): Boolean {
        return classesInformation.size == 2
    }
}
