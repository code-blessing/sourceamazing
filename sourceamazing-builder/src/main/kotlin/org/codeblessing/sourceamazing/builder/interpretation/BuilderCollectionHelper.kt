package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.schema.type.KTypeUtil.KTypeClassInformation
import kotlin.reflect.full.starProjectedType

object BuilderCollectionHelper {
    val SUPPORTED_COLLECTION_TYPES = setOf(List::class.starProjectedType, Set::class.starProjectedType, Array::class.starProjectedType)

    /**
     * keep in sync with [SUPPORTED_COLLECTION_TYPES]
     */
    fun facetValueListFromFacetValue(value: Any): List<Any> {
        // having null values here should throw an exception if not @IgnoreNullValues
        // but as we don't allow nullable inner values in collection, this is never the case
        return when(value) {
            is List<*> -> value.filterNotNull()
            is Set<*> -> value.filterNotNull().toList()
            is Array<*> -> value.filterNotNull().toList()
            else -> listOf(value)
        }
    }

    fun extractValueClassFromCollectionIfCollection(
        classesInformation: List<KTypeClassInformation>,
        methodLocation: MethodLocation,
    ): KTypeClassInformation {
        val valueClassOrCollectionClass = classesInformation.first()
        return if(valueClassOrCollectionClass.clazz.starProjectedType in SUPPORTED_COLLECTION_TYPES) {
            if(valueClassOrCollectionClass.isValueNullable) {
                throw BuilderMethodSyntaxException(methodLocation, BuilderErrorCode.BUILDER_PARAM_NO_NULLABLE_COLLECTION_TYPE)
            }
            classesInformation.last()
        } else {
            valueClassOrCollectionClass
        }
    }


}