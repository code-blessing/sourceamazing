package org.codeblessing.sourceamazing.schema.fakereflection

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

class FakeKType(override val classifier: KClassifier?) : FakeKAnnotatedElement<FakeKType>(), KType {
    private val internalArguments: MutableList<KTypeProjection> = mutableListOf()
    private var internalMarkedNullable: Boolean = false

    override val arguments: List<KTypeProjection> get() = internalArguments
    override val isMarkedNullable: Boolean get() = internalMarkedNullable

    companion object {
        fun createClassType(clazz: KClass<*>, nullable: Boolean = false): FakeKType {
            return FakeKType(clazz).nullable(nullable)
        }

        fun createFunctionType(function: KFunction<*>, nullable: Boolean = false): FakeKType {
            // TODO add parameters as kType arguments
            return FakeKType(function::class).nullable(nullable)
        }

    }

    fun nullable(isNullable: Boolean): FakeKType {
        internalMarkedNullable = isNullable
        return this
    }

}