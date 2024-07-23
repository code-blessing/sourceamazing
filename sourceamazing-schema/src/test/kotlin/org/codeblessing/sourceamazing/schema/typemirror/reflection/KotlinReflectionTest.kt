package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.javaType

class KotlinReflectionTest {

    interface ReflectionPlaygroundInterface<T> {
        fun myMethod(stringValue: String, myLambda: (String, Int, Double) -> Boolean, myList: List<String>, bar: String.() -> Boolean)
    }

    class ReflectionPlaygroundInterfaceInstance : ReflectionPlaygroundInterface<String> {
        override fun myMethod(stringValue: String, myLambda: (String, Int, Double) -> Boolean, myList: List<String>, bar: String.() -> Boolean) {
            TODO("Not yet implemented")
        }

        fun <X> myXParam(foo: X) {
            TODO("Not yet implemented")
        }
    }

    fun ReflectionPlaygroundInterface<String>.foo() {
        // to something

    }

    private fun printKClass(clazz: KClass<*>) {
        println("Class: $clazz")
        clazz.typeParameters.forEach {
            printKTypeParameter(it)
        }
        clazz.members.filter { it.name.startsWith("my") }.forEach { kCallable ->
            println(" Member: $kCallable")
            kCallable.typeParameters.forEach { printKTypeParameter(it) }
            kCallable.parameters.forEach {
                printKParameter(it)
            }


        }

    }

    private fun printKTypeParameter(param: KTypeParameter) {
        println("  TypeParameter: ${param.name}")
    }

    private fun printKParameter(param: KParameter) {
        println("  Parameter: ${param.index} ${param.kind} ${param.name}")
        printKType(param.type)
    }

    private fun printKType(type: KType) {
        println("    KType: $type")
        println("    Null?: ${type.isMarkedNullable}")
        type.arguments.forEachIndexed { index, kType ->
            println("      Type arguments ${index}: ${kType.type} ${kType.variance} ")
            kType.type?.let { printKType(it) }


        }
        val classifier = type.classifier ?: return
        println("    Classifier: ${classifier}")
        when(classifier) {
            is KTypeParameter -> printKTypeParameter(classifier)
            is KClass<*> -> println("    KClass: $classifier")
        }
    }

    @Test
    fun `test reflection of kotlin`() {

        val interfaceClazz = ReflectionPlaygroundInterface::class

        val interfaceInstanceClazz = ReflectionPlaygroundInterfaceInstance::class

        Assertions.assertEquals("ReflectionPlaygroundInterface", interfaceClazz.simpleName)
//        Assertions.assertEquals("ReflectionPlaygroundInterfaceInstance", interfaceInstanceClazz.simpleName)

        printKClass(interfaceClazz)
        printKClass(interfaceInstanceClazz)
    }

}