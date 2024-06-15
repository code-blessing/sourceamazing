package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.ClassTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionTypeMirrorInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.reflect.KFunction
import kotlin.reflect.KType

/**
 * TODO Welche Fälle sind noch nicht getestet?
 *      Felder mit Map<String, List<Int>>
 *      Felder mit List<() -> Int>
 *      Receiver-Objekte?
 *      Superklassen/Klassenhierarchie
 *      this-Objekt bei Methoden?
 *      Funktioniert der Mechanismus über tiefer Verschachtelungen?
 *      Funktioniert der Mechanismus mit Kotlin-Objekten (String, List, etc.)
 *      Werden type-parameter konsequent unterbunden?
 */
class PlaygroundTest {

    @Test
    fun `test type arguments and parameters`() {
        val stringList = arrayListOf<String>()
        val typeParams = stringList::class.typeParameters
        println("Result type params: $typeParams")

        abstract class MyClass {
            abstract fun <T: Number>doSomething(): Map<T, List<Boolean>>
        }
        val method:KFunction<Map<Double, List<Boolean>>> = MyClass::doSomething
        val returnTypeOfMethod: KType = method.returnType
        //method.parameters
        printType(returnTypeOfMethod, 0)
    }

    private fun printType(kType: KType, ident: Int) {
        val space = " ".repeat(ident)
        println("${space}$kType (${kType.classifier})")
        kType.arguments.forEach { kTypeProjection ->
            val subType = kTypeProjection.type
            if(subType != null) {
                printType(subType, ident + 1)
            } else {
                println("no sub type")
            }

        }
    }

    @Test
    fun `function type parameters is valid`() {
        abstract class AClass {
            abstract fun myFunctionType(): () -> String
        }
        val typeMirror = JavaReflectionMirrorFactory.createTypeMirrorProvider(AClass::myFunctionType.returnType)
        val functionTypeClassMirror = typeMirror.provideMirror() as FunctionTypeMirrorInterface
        val function = functionTypeClassMirror.functionMirror.provideMirror()
        assertEquals(0, function.valueParameters.size)
        assertNull(function.receiverParameterType)
        assertNull(function.instanceParameterType)
        assertNotNull(function.returnType)
        val stringClassMirror = (requireNotNull(function.returnType).type as ClassTypeMirrorInterface).classMirror.provideMirror()
        assertEquals("kotlin.String", stringClassMirror.fullQualifiedName)
    }

    @Test
    fun `class type parameter is valid`() {
//        abstract class AClassWithNestedGenericParameters {
//            abstract fun myFunctionType(): Map<String, Map<Boolean, List<Int>>>
//            abstract fun myFunctionWithLambdaType(): Map<String?, Map<() -> Boolean, List<Int>>>
//        }
//        val typeMirror = JavaReflectionMirrorFactory.createSignatureMirrorProvider(AClassWithNestedGenericParameters::myFunctionType.returnType)
//        val mapClassMirror = typeMirror.provideMirror() as ClassMirrorInterface
//        assertEquals("kotlin.collections.Map", mapClassMirror.fullQualifiedName)
//        assertEquals(2, mapClassMirror.typeParameters.size)
//        assertEquals(listOf("K, V").joinToString(), mapClassMirror.typeParameters.joinToString { it.name })
        //assertClassMirrorType("kotlin.collections.Map", typeMirror.provideMirror())


//        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AClassWithNestedGenericParameters::class)
//        classMirror.withMethod(AClassWithNestedGenericParameters::myFunctionType).let { method ->
//            assertClassMirrorType("kotlin.collections.Map", method.returnType?.type)
//            val mapClassMirror = method.returnType?.type?.signatureMirror?.provideMirror() as ClassMirrorInterface
//
//            assertEquals(2, mapClassMirror.typeParameters.size)
//        }
    }

}