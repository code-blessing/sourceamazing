package org.codeblessing.sourceamazing.schema.utils.type

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

object KFunctionUtil {

    /**
     * Returns the given [baseFunction] or a found function of the [derivedClass] if the function is the same but
     * overloaded (derived).
     */
    fun functionOrDerivedFunction(baseFunction: KFunction<*>, derivedClass: KClass<*>): KFunction<*> {
        return derivedClass.memberFunctions.firstOrNull { derivedFunction ->
            isSameOrDerived(baseFunction, derivedFunction)
        } ?: baseFunction
    }

    private fun isSameOrDerived(base: KFunction<*>, derived: KFunction<*>): Boolean {
        val b = base.javaMethod ?: return false
        val d = derived.javaMethod ?: return false

        // Must have the same name and parameter types
        if (d.name != b.name) return false
        if (!d.parameterTypes.contentEquals(b.parameterTypes)) return false

        // Return true if derived's declaring class is a subclass of base's declaring class
        return b == d || b.declaringClass.isAssignableFrom(d.declaringClass)
    }
}
