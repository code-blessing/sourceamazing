package org.codeblessing.sourceamazing.builder.interpretation

import kotlin.reflect.full.starProjectedType
import org.codeblessing.sourceamazing.builder.interpretation.BuilderCollectionHelper.SUPPORTED_COLLECTION_TYPES

object BuilderCollectionHelper {
    val SUPPORTED_COLLECTION_TYPES =
        setOf(List::class.starProjectedType, Set::class.starProjectedType, Array::class.starProjectedType)

    /** keep in sync with [SUPPORTED_COLLECTION_TYPES] */
    fun clazzPropertyValueListFromClazzPropertyValue(value: Any): List<Any> {
        // having null values here should throw an exception if not @IgnoreNullValues
        // but as we don't allow nullable inner values in collection, this is never the case
        return when (value) {
            is List<*> -> value.filterNotNull()
            is Set<*> -> value.filterNotNull().toList()
            is Array<*> -> value.filterNotNull().toList()
            else -> listOf(value)
        }
    }
}
