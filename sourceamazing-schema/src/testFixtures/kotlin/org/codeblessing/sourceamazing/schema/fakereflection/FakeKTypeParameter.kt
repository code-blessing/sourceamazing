package org.codeblessing.sourceamazing.schema.fakereflection

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVariance

class FakeKTypeParameter(override val name: String, override val variance: KVariance) : FakeKAnnotatedElement<FakeKTypeParameter>(), KTypeParameter {
    private val internalUpperBounds: MutableList<KType> = mutableListOf()
    private var internalIsReified: Boolean = false

    override val isReified: Boolean get() = internalIsReified
    override val upperBounds: List<KType> get() = internalUpperBounds

    fun reified(isReified: Boolean): FakeKTypeParameter {
        internalIsReified = isReified
        return this
    }

    fun withUpperBound(upperBound: KType): FakeKTypeParameter {
        internalUpperBounds.add(upperBound)
        return this
    }

}