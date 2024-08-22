package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KFunction

class JavaReflectionMirrorFactoryTest {
    private val numberOfDefaultMethods = 3 //toString(), hash(), equals()

    @Schema(concepts = [MyConceptInterface::class])
    private interface MySchemaInterface

    @Concept(facets = [MyFacetInterface::class])
    private interface MyConceptInterface

    @StringFacet
    private interface MyFacetInterface {
        @Suppress("UNUSED") fun giveMeTheConcept(): MyConceptInterface
    }


    @Test
    @Deprecated("This was a smoke test. Do we still need that?")
    fun convertToTypeMirror() {
        val mySchemaInterfaceMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(MySchemaInterface::class)
        assertEquals(1, mySchemaInterfaceMirror.annotations.size)
        assertEquals(3, mySchemaInterfaceMirror.methods.size)

        val schemaAnnotationMirror = mySchemaInterfaceMirror.annotations[0] as SchemaAnnotationMirror
        assertEquals(1, schemaAnnotationMirror.concepts.size)
        val conceptInterfaceMirror = schemaAnnotationMirror.concepts[0].provideMirror()
        assertEquals(1, conceptInterfaceMirror.annotations.size)
        val conceptAnnotationMirror = conceptInterfaceMirror.annotations[0] as ConceptAnnotationMirror
        assertEquals(1, conceptAnnotationMirror.facets.size)
        val facetInterfaceMirror = conceptAnnotationMirror.facets[0].provideMirror()
        assertEquals(4, facetInterfaceMirror.methods.size)
        val giveMeTheConceptMethodMirror = facetInterfaceMirror.methods[0]
        val againConceptInterface = requireNotNull(giveMeTheConceptMethodMirror.returnType).type.signatureMirror.provideMirror()
        require(againConceptInterface is ClassMirrorInterface)
        assertEquals(conceptInterfaceMirror.classQualifier, againConceptInterface.classQualifier)
        assertFalse(conceptInterfaceMirror.classQualifier === againConceptInterface.classQualifier)
    }

    private interface AnEmptyInterface

    @Test
    fun `interface is marked as an interface and no other class type`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnEmptyInterface::class)
        assertEquals(false, classMirror.isAnnotation)
        assertEquals(false, classMirror.isClass)
        assertEquals(false, classMirror.isDataClass)
        assertEquals(false, classMirror.isEnum)
        assertEquals(true, classMirror.isInterface)
        assertEquals(false, classMirror.isObjectClass)
    }

    private class AnEmptyClass

    @Test
    fun `class is marked as a class and no other class type`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnEmptyClass::class)
        assertEquals(false, classMirror.isAnnotation)
        assertEquals(true, classMirror.isClass)
        assertEquals(false, classMirror.isDataClass)
        assertEquals(false, classMirror.isEnum)
        assertEquals(false, classMirror.isInterface)
        assertEquals(false, classMirror.isObjectClass)
    }

    private annotation class AnAnnotationClass

    @Test
    fun `annotation class is marked as an annotation class and no other class type`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnAnnotationClass::class)
        assertEquals(true, classMirror.isAnnotation)
        assertEquals(false, classMirror.isClass)
        assertEquals(false, classMirror.isDataClass)
        assertEquals(false, classMirror.isEnum)
        assertEquals(false, classMirror.isInterface)
        assertEquals(false, classMirror.isObjectClass)
    }

    private enum class AnEnumClass {
        FOO, BAR, BAZ
    }

    @Test
    fun `enum class is marked as an enum class and no other class type`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnEnumClass::class)
        assertEquals(false, classMirror.isAnnotation)
        assertEquals(false, classMirror.isClass)
        assertEquals(false, classMirror.isDataClass)
        assertEquals(true, classMirror.isEnum)
        assertEquals(false, classMirror.isInterface)
        assertEquals(false, classMirror.isObjectClass)
    }

    @Test
    fun `enum values of an enum class are represented as string values in the correct order`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnEnumClass::class)
        assertEquals(true, classMirror.isEnum)
        val expectedEnumValues = listOf(
            AnEnumClass.FOO.toString(),
            AnEnumClass.BAR.toString(),
            AnEnumClass.BAZ.toString(),
        )
        assertEquals(expectedEnumValues, classMirror.enumValues)
    }

    @Schema(concepts = [AnEmptyInterface::class])
    @StringFacet
    @BooleanFacet(minimumOccurrences = 5, maximumOccurrences = 8)
    @Concept(facets = [AnEmptyInterface::class, AnEmptyInterface::class, AnEmptyInterface::class])
    private interface AnInterfaceWithMultipleAnnotations

    @Test
    fun `annotations and their values on a interface are represented correctly`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMultipleAnnotations::class)
        assertEquals(4, classMirror.annotations.size)

        val schemaAnnotationMirror = classMirror.annotations[0] as SchemaAnnotationMirror
        assertEquals(1, schemaAnnotationMirror.concepts.size)
        assertEquals(AnEmptyInterface::class.simpleName, schemaAnnotationMirror.concepts.first().provideMirror().className)

        val stringFacetAnnotationMirror = classMirror.annotations[1] as StringFacetAnnotationMirror
        assertEquals(true, stringFacetAnnotationMirror.isAnnotation(StringFacet::class))
        assertEquals(false, stringFacetAnnotationMirror.isAnnotation(BooleanFacet::class))
        assertEquals(false, stringFacetAnnotationMirror.isAnnotation(Deprecated::class))
        assertEquals(1, stringFacetAnnotationMirror.minimumOccurrences)
        assertEquals(1, stringFacetAnnotationMirror.maximumOccurrences)

        val booleanFacetAnnotationMirror = classMirror.annotations[2] as BooleanFacetAnnotationMirror
        assertEquals(false, booleanFacetAnnotationMirror.isAnnotation(StringFacet::class))
        assertEquals(true, booleanFacetAnnotationMirror.isAnnotation(BooleanFacet::class))
        assertEquals(false, booleanFacetAnnotationMirror.isAnnotation(Deprecated::class))
        assertEquals(5, booleanFacetAnnotationMirror.minimumOccurrences)
        assertEquals(8, booleanFacetAnnotationMirror.maximumOccurrences)

        val conceptFacetAnnotationMirror = classMirror.annotations[3] as ConceptAnnotationMirror
        assertEquals(3, conceptFacetAnnotationMirror.facets.size)
    }

    private interface AnInterfaceWithMethodsInACertainOrder {

        fun firstMethod()
        fun secondMethod()
        fun thirdMethod()
        fun fourthMethod()
    }


    @Test
    fun `methods on a interface are represented but not necessary in the correct order`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMethodsInACertainOrder::class)
        assertEquals(numberOfDefaultMethods + 4, classMirror.methods.size)

        val expectedMethods = setOf(
            AnInterfaceWithMethodsInACertainOrder::firstMethod.name,
            AnInterfaceWithMethodsInACertainOrder::secondMethod.name,
            AnInterfaceWithMethodsInACertainOrder::thirdMethod.name,
            AnInterfaceWithMethodsInACertainOrder::fourthMethod.name,
            AnInterfaceWithMethodsInACertainOrder::equals.name,
            AnInterfaceWithMethodsInACertainOrder::hashCode.name,
            AnInterfaceWithMethodsInACertainOrder::toString.name,
        )
        assertTrue(expectedMethods.containsAll(classMirror.methods.mapNotNull { it.functionName }))
    }

    private interface AnInterfaceWithMethodsWithoutParamsAndReturnType {

        fun methodWithoutParamsAndReturnType()
    }

    @Test
    fun `methods without param and return types on a interface are represented correctly`() {
        val classMirror =
            JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMethodsWithoutParamsAndReturnType::class)
        assertEquals(numberOfDefaultMethods + 1, classMirror.methods.size)

        classMirror.withMethod(AnInterfaceWithMethodsWithoutParamsAndReturnType::methodWithoutParamsAndReturnType).let { method ->
            assertEquals(0, method.parameters.size)
            assertNull(method.returnType)
        }
    }

    private interface AnInterfaceWithMethodsWithParams {
        class AParamObject

        fun methodWithSimpleParam(objectParam: AParamObject)
        fun methodWithSimpleNullableParam(objectParam: AParamObject?)
        fun methodWithListParams(listParam: List<AParamObject>)
    }

    @Test
    fun `methods and their params on a interface are represented correctly`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMethodsWithParams::class)
        assertEquals(numberOfDefaultMethods + 3, classMirror.methods.size)

        classMirror.withMethod(AnInterfaceWithMethodsWithParams::methodWithSimpleParam).let { method ->
            assertEquals(1, method.parameters.size)
            method.parameters.first().let { param ->
                assertEquals(false, param.type.nullable)
                assertEquals(true, param.type.signatureMirror.provideMirror() is ClassMirrorInterface)
                val returnTypeClass = param.type.signatureMirror.provideMirror() as ClassMirrorInterface
                assertEquals(AnInterfaceWithMethodsWithParams.AParamObject::class.qualifiedName, returnTypeClass.fullQualifiedName)
            }
        }
        classMirror.withMethod(AnInterfaceWithMethodsWithParams::methodWithSimpleNullableParam).let { method ->
            assertEquals(1, method.parameters.size)
            method.parameters.first().let { param ->
                assertEquals(true, param.type.nullable)
            }
        }
        classMirror.withMethod(AnInterfaceWithMethodsWithParams::methodWithListParams).let { method ->
            assertEquals(1, method.parameters.size)
            method.parameters.first().let { param ->
                assertEquals(false, param.type.nullable)
                assertEquals(true, param.type.signatureMirror.provideMirror() is ClassMirrorInterface)
                val returnTypeClass = param.type.signatureMirror.provideMirror() as ClassMirrorInterface
                assertEquals("kotlin.collections.List", returnTypeClass.fullQualifiedName)

            }
        }
    }

    private interface AnInterfaceWithMethodsReturningValues {

        class AReturnedObject

        fun methodReturningASimpleNullableObject(): AReturnedObject?
        fun methodReturningASimpleObject(): AReturnedObject
        fun methodReturningAList(): List<AReturnedObject>
        fun methodWithoutReturnType()
    }

    @Test
    fun `methods return types on a interface are represented correctly`() {
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMethodsReturningValues::class)
        assertEquals(numberOfDefaultMethods + 4, classMirror.methods.size)

        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodWithoutReturnType).let { method ->
            assertNull(method.returnType)
        }
        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodReturningASimpleObject).let { method ->
            method.returnType!!.let { returnType ->
                assertEquals(false, returnType.type.nullable)
                assertEquals(true, returnType.type.signatureMirror.provideMirror() is ClassMirrorInterface)
                val returnTypeClass = returnType.type.signatureMirror.provideMirror() as ClassMirrorInterface
                assertEquals(AnInterfaceWithMethodsReturningValues.AReturnedObject::class.qualifiedName, returnTypeClass.fullQualifiedName)
            }
        }
        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodReturningASimpleNullableObject).let { method ->
            assertEquals(true, method.returnType!!.type.nullable)
        }
        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodReturningAList).let { method ->
            method.returnType!!.let { returnType ->
                assertEquals(false, returnType.type.nullable)
                assertEquals(true, returnType.type.signatureMirror.provideMirror() is ClassMirrorInterface)
                val returnTypeClass = returnType.type.signatureMirror.provideMirror() as ClassMirrorInterface
                assertEquals(List::class.qualifiedName, returnTypeClass.fullQualifiedName)
                // TODO inspect the generic param of the list
            }
        }
    }

    interface InterfaceWithMethodsInheritedFromAnyObject

    @Test
    fun `represents the interface methods inherited from kotlin Any`() {
        val myInterfaceMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(InterfaceWithMethodsInheritedFromAnyObject::class)
        assertEquals(numberOfDefaultMethods, myInterfaceMirror.methods.size)

        val toStringMethod = myInterfaceMirror.methods.first { it.functionName == InterfaceWithMethodsInheritedFromAnyObject::toString.name }
        assertEquals("toString", toStringMethod.functionName)
        assertEquals(0, toStringMethod.parameters.size)
        toStringMethod.returnType!!.let { returnType ->
            assertEquals(false, returnType.type.nullable)
            val returnTypeClassMirror = returnType.type.signatureMirror.provideMirror() as ClassMirrorInterface
            assertEquals("kotlin.String", returnTypeClassMirror.fullQualifiedName)
        }
    }

    private fun ClassMirrorInterface.withMethod(kFunction: KFunction<*>): FunctionMirrorInterface {
        return this.methods.first { it.functionName == kFunction.name }
    }
}