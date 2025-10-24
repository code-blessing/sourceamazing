package org.codeblessing.sourceamazing.schema.utils.typedeconstruction

import kotlin.reflect.KClass

data class TypeDeconstructionData(
    val valueClass: KClass<*>,
    val isValueNullable: Boolean,
    val collectionClass: KClass<*>?,
    val isCollectionNullable: Boolean?,
)
