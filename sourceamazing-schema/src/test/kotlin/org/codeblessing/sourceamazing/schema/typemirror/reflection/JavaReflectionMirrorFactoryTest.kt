package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ClassTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.EveryLocationAnnotation
import org.codeblessing.sourceamazing.schema.typemirror.FieldMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.OtherAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptsAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeHelper
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeParameterTypeMirrorInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

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
class JavaReflectionMirrorFactoryTest {
    private val numberOfDefaultMethods = TypeHelper.kotlinAnyClassMethodNames.size //toString(), hash(), equals()

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

    @Test
    fun `class is marked as a class and no other class type`() {
        class AnEmptyClass
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

    @Test
    fun `annotations and their values on a class are represented correctly`() {

        @Schema(concepts = [AnEmptyInterface::class])
        @StringFacet
        @BooleanFacet(minimumOccurrences = 5, maximumOccurrences = 8)
        @Concept(facets = [AnEmptyInterface::class, AnEmptyInterface::class, AnEmptyInterface::class])
        class AnClassWithMultipleAnnotations

        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnClassWithMultipleAnnotations::class)
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

    @Test
    fun `methods on a class are represented but not necessary in the correct order`() {

        abstract class AnInterfaceWithMethodsInACertainOrder {

            abstract fun firstMethod()
            abstract fun secondMethod()
            abstract fun thirdMethod()
            abstract fun fourthMethod()
        }

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

    @Test
    fun `method annotations are reflected correctly`() {
        abstract class AnInterfaceWithMethodsHavingAnnotations {

            @QueryConceptIdentifierValue
            @QueryConcepts(conceptClasses = [])
            @Deprecated("This is deprecated")
            abstract fun methodWithAnnotation()

            abstract fun methodWithoutAnnotation()
        }

        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMethodsHavingAnnotations::class)
        assertEquals(numberOfDefaultMethods + 2, classMirror.methods.size)

        classMirror.withMethod(AnInterfaceWithMethodsHavingAnnotations::methodWithoutAnnotation).let { method ->
            assertEquals(0, method.annotations.size)
        }

        classMirror.withMethod(AnInterfaceWithMethodsHavingAnnotations::methodWithAnnotation).let { method ->
            assertEquals(3, method.annotations.size)

            val queryConceptsIdentifierValueAnnotationMirror = method.annotations[0] as QueryConceptIdentifierValueAnnotationMirror
            assertEquals(true, queryConceptsIdentifierValueAnnotationMirror.isAnnotation(QueryConceptIdentifierValue::class))

            val queryConceptsAnnotationMirror = method.annotations[1] as QueryConceptsAnnotationMirror
            assertEquals(true, queryConceptsAnnotationMirror.isAnnotation(QueryConcepts::class))

            val deprecatedAnnotationMirror = method.annotations[2] as OtherAnnotationMirror
            assertEquals(true, deprecatedAnnotationMirror.isAnnotation(Deprecated::class))

        }
    }

    @Test
    fun `methods without param and return types on a class are represented correctly`() {
        abstract class AnInterfaceWithMethodsWithoutParamsAndReturnType {
            abstract fun methodWithoutParamsAndReturnType()
        }

        val classMirror =
            JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMethodsWithoutParamsAndReturnType::class)
        assertEquals(numberOfDefaultMethods + 1, classMirror.methods.size)

        classMirror.withMethod(AnInterfaceWithMethodsWithoutParamsAndReturnType::methodWithoutParamsAndReturnType).let { method ->
            assertEquals(0, method.valueParameters.size)
            assertNull(method.returnType)
        }
    }

    @Test
    fun `method parameters on a class are represented correctly`() {
        class AParamObject
        abstract class AClassWithMethodsWithParams {


            abstract fun methodWithSimpleParam(objectParam: AParamObject)
            abstract fun methodWithSimpleNullableParam(objectParam: AParamObject?)
            abstract fun methodWithListParams(listParam: List<AParamObject>)
            abstract fun methodWithKotlinBuiltIntType(myInt: Int)
            abstract fun methodWithAnnotatedParam(@EveryLocationAnnotation myInt: Int)
        }

        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AClassWithMethodsWithParams::class)
        assertEquals(numberOfDefaultMethods + 5, classMirror.methods.size)

        classMirror.withMethod(AClassWithMethodsWithParams::methodWithSimpleParam).let { method ->
            assertEquals(1, method.valueParameters.size)
            method.valueParameters.first().let { param ->
                assertEquals(false, param.type.nullable)
                assertEquals(true, param.type is ClassTypeMirrorInterface)
                val returnTypeClass = (param.type as ClassTypeMirrorInterface).classMirror.provideMirror() as ClassMirrorInterface
                assertEquals("AParamObject", returnTypeClass.fullQualifiedName)
            }
        }
        classMirror.withMethod(AClassWithMethodsWithParams::methodWithSimpleNullableParam).let { method ->
            assertEquals(1, method.valueParameters.size)
            method.valueParameters.first().let { param ->
                assertEquals(true, param.type.nullable)
            }
        }
        classMirror.withMethod(AClassWithMethodsWithParams::methodWithListParams).let { method ->
            assertEquals(1, method.valueParameters.size)
            method.valueParameters.first().let { param ->
                assertEquals(false, param.type.nullable)
                assertEquals(true, param.type is ClassTypeMirrorInterface)
                val returnTypeClass = (param.type as ClassTypeMirrorInterface).classMirror.provideMirror()
                assertEquals("kotlin.collections.List", returnTypeClass.fullQualifiedName)

            }
        }
        classMirror.withMethod(AClassWithMethodsWithParams::methodWithKotlinBuiltIntType).let { method ->
            assertEquals(1, method.valueParameters.size)
            method.valueParameters.first().let { param ->
                assertEquals(false, param.type.nullable)
                assertEquals(true, param.type is ClassTypeMirrorInterface)
                val returnTypeClass = (param.type as ClassTypeMirrorInterface).classMirror.provideMirror()
                assertEquals("kotlin.Int", returnTypeClass.fullQualifiedName)
            }
        }
        classMirror.withMethod(AClassWithMethodsWithParams::methodWithAnnotatedParam).let { method ->
            assertEquals(1, method.valueParameters.size)
            method.valueParameters.first().let { param ->
                assertEquals(1, param.annotations.size)
                val annotation = param.annotations.first() as OtherAnnotationMirror
                assertEquals(true, annotation.isAnnotation(EveryLocationAnnotation::class))
            }
        }
    }

    @Test
    fun `fields on a class are represented correctly`() {
        class AFieldObject
        abstract class AnClassWithFields {
            abstract val myInt: Int
            abstract val mySimpleObject: AFieldObject?
            abstract val myMethodField: () -> Int?
            abstract val myOtherField: List<Int>
        }

        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnClassWithFields::class)
        assertEquals(4, classMirror.fields.size)

        classMirror.withField(AnClassWithFields::myInt).let { field ->
            assertEquals("myInt", field.fieldName)
            assertEquals(false, field.type.nullable)
            assertEquals(false, field.type.nullable)
            assertClassMirrorType("kotlin.Int", field.type)
        }
        classMirror.withField(AnClassWithFields::mySimpleObject).let { field ->
            assertEquals("mySimpleObject", field.fieldName)
            assertEquals(true, field.type.nullable)
            assertClassMirrorType("AFieldObject", field.type)
        }
        classMirror.withField(AnClassWithFields::myMethodField).let { field ->
            assertEquals("myMethodField", field.fieldName)
            assertEquals(false, field.type.nullable)
            assertEquals(true, field.type is FunctionTypeMirrorInterface)
            val fieldFunction = (field.type as FunctionTypeMirrorInterface).functionMirror.provideMirror()
            assertNull(fieldFunction.functionName)
            assertNull(fieldFunction.instanceParameterType)
            assertNull(fieldFunction.receiverParameterType)
            assertEquals(true, fieldFunction.valueParameters.isEmpty())
            assertNotNull(fieldFunction.returnType)
            val returnType = requireNotNull(fieldFunction.returnType)
            assertEquals(true, returnType.type.nullable)
            assertClassMirrorType("kotlin.Int", returnType.type)
        }
        classMirror.withField(AnClassWithFields::myOtherField).let { field ->
            assertEquals("myOtherField", field.fieldName)
            assertEquals(false, field.type.nullable)
            assertClassMirrorType("kotlin.collections.List", field.type)
        }
    }

    @Test
    fun `type parameters on fields are represented as type parameter types`() {
        abstract class AClassWithForTypesWithTypeParams<E> {
            abstract val myTypeParamField: List<E>
        }

        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AClassWithForTypesWithTypeParams::class)
        classMirror.withField(AClassWithForTypesWithTypeParams<*>::myTypeParamField).let { field ->
            assertEquals("myTypeParamField", field.fieldName)
            assertEquals(false, field.type.nullable)
            assertEquals(true, field.type is TypeParameterTypeMirrorInterface)
        }
    }

    @Test
    fun `type parameters on methods are represented as type parameter types`() {
        abstract class AClassWithTypeParametersOnMethod {
            abstract fun <E> myGenericReturningMethod(): List<E>
        }
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AClassWithTypeParametersOnMethod::class)
        val methodReference: KFunction<List<Int>> = AClassWithTypeParametersOnMethod::myGenericReturningMethod
        classMirror.withMethod(methodReference).let { method ->
            assertEquals("myGenericReturningMethod", method.functionName)
            val returnType = requireNotNull(method.returnType?.type)
            assertEquals(true, returnType is TypeParameterTypeMirrorInterface)
        }
    }

    @Test
    fun `method return types on a class are represented correctly`() {
        class AReturnedObject
        abstract class AnInterfaceWithMethodsReturningValues {
            abstract fun methodReturningASimpleNullableObject(): AReturnedObject?
            abstract fun methodReturningASimpleObject(): AReturnedObject
            abstract fun methodReturningAList(): List<AReturnedObject>
            abstract fun methodWithoutReturnType()
        }


        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterfaceWithMethodsReturningValues::class)
        assertEquals(numberOfDefaultMethods + 4, classMirror.methods.size)

        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodWithoutReturnType).let { method ->
            assertNull(method.returnType)
        }
        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodReturningASimpleObject).let { method ->
            method.returnType!!.let { returnType ->
                assertEquals(false, returnType.type.nullable)
                assertEquals(true, returnType.type is ClassTypeMirrorInterface)
                val returnTypeClass = (returnType.type as ClassTypeMirrorInterface).classMirror.provideMirror()
                assertEquals("AReturnedObject", returnTypeClass.fullQualifiedName)
            }
        }
        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodReturningASimpleNullableObject).let { method ->
            assertEquals(true, method.returnType!!.type.nullable)
        }
        classMirror.withMethod(AnInterfaceWithMethodsReturningValues::methodReturningAList).let { method ->
            method.returnType!!.let { returnType ->
                assertEquals(false, returnType.type.nullable)
                assertEquals(true, returnType.type is ClassTypeMirrorInterface)
                val returnTypeClass = (returnType.type as ClassTypeMirrorInterface).classMirror.provideMirror()
                assertEquals(List::class.qualifiedName, returnTypeClass.fullQualifiedName)
                // TODO inspect the generic param of the list
            }
        }
    }

    @Test
    fun `class mirror represents the class methods inherited from kotlin Any`() {
        class ClassWithMethodsInheritedFromAnyObject

        val myInterfaceMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(ClassWithMethodsInheritedFromAnyObject::class)
        assertEquals(numberOfDefaultMethods, myInterfaceMirror.methods.size)

        val toStringMethod = myInterfaceMirror.methods.first { it.functionName == ClassWithMethodsInheritedFromAnyObject::toString.name }
        assertEquals("toString", toStringMethod.functionName)
        assertEquals(0, toStringMethod.valueParameters.size)
        toStringMethod.returnType!!.let { returnType ->
            assertEquals(false, returnType.type.nullable)
            assertClassMirrorType("kotlin.String", returnType.type)
        }
    }


    @Test
    fun `type arguments and return values of an anonymous class are distributed correctly in the function type object`() {
        abstract class AnClassWithFunctionTypes {
            abstract fun myFunctionType(): String.(Boolean?, Double) -> Int?
        }

        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnClassWithFunctionTypes::class)
        val typeMirror = requireNotNull(classMirror.withMethod(AnClassWithFunctionTypes::myFunctionType).returnType?.type).provideMirror()

        assertEquals(true, typeMirror is FunctionTypeMirrorInterface)
        val functionTypeMirror = (typeMirror as FunctionTypeMirrorInterface).functionMirror.provideMirror()
        assertNull(functionTypeMirror.instanceParameterType)

        assertNotNull(functionTypeMirror.receiverParameterType)
        val receiverParameter = requireNotNull(functionTypeMirror.receiverParameterType)
        assertNull(receiverParameter.name)
        assertEquals(false, receiverParameter.type.nullable)
        assertClassMirrorType("kotlin.String", receiverParameter.type)

        assertEquals(2, functionTypeMirror.valueParameters.size)
        val firstValueParameter = functionTypeMirror.valueParameters.first()
        assertNull(firstValueParameter.name)
        assertEquals(true, firstValueParameter.type.nullable)
        assertClassMirrorType("kotlin.Boolean", firstValueParameter.type)

        val secondValueParameter = functionTypeMirror.valueParameters.last()
        assertNull(secondValueParameter.name)
        assertEquals(false, secondValueParameter.type.nullable)
        assertClassMirrorType("kotlin.Double", secondValueParameter.type)

        assertNotNull(functionTypeMirror.receiverParameterType)
        val returnType = requireNotNull(functionTypeMirror.returnType)
        assertNotNull(returnType.type)
        assertEquals(true, returnType.type.nullable)
        assertClassMirrorType("kotlin.Int", returnType.type)

    }

    @Test
    fun `type parameters of a kotlin class is provided`() {
        abstract class AClassWithNestedGenericParameters {
            abstract fun mapReturningFunction(): Map<String, Map<Boolean, List<Int>>>
        }
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AClassWithNestedGenericParameters::class)
        val function: KFunction<Map<*, *>> = AClassWithNestedGenericParameters::mapReturningFunction
        val typeMirror = requireNotNull(classMirror.withMethod(function).returnType?.type).provideMirror()
        val mapClassMirror = typeMirror.provideMirror() as ClassMirrorInterface
        assertEquals("kotlin.collections.Map", mapClassMirror.fullQualifiedName)
        assertEquals(2, mapClassMirror.typeParameters.size)
        assertEquals(listOf("K, V").joinToString(), mapClassMirror.typeParameters.joinToString { it.name })
    }

    @Test
    fun `type parameters of a custom class is provided`() {
        abstract class AClassWithGenericParameters<T, V> {
            abstract fun mapReturningFunction(): Map<T, V>
        }
        val classMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AClassWithGenericParameters::class)
        val function: KFunction<Map<*, *>> = AClassWithGenericParameters<*, *>::mapReturningFunction
        val typeMirror = requireNotNull(classMirror.withMethod(function).returnType?.type).provideMirror()

        val mapClassMirror = typeMirror.provideMirror() as ClassMirrorInterface
        assertEquals("kotlin.collections.Map", mapClassMirror.fullQualifiedName)
        assertEquals(2, mapClassMirror.typeParameters.size)
        assertEquals(listOf("K, V").joinToString(), mapClassMirror.typeParameters.joinToString { it.name })
    }

    private fun assertClassMirrorType(fullQualifiedName: String, typeMirrorInterface: TypeMirrorInterface?) {
        assertNotNull(typeMirrorInterface, "Expected type of class $fullQualifiedName but was null.")
        requireNotNull(typeMirrorInterface)
        assertTrue(typeMirrorInterface is ClassMirrorInterface, "Expected $typeMirrorInterface to contain a ClassMirror.")
        val classMirror = typeMirrorInterface as ClassTypeMirrorInterface
        assertEquals(fullQualifiedName, classMirror.classMirror.provideMirror().fullQualifiedName)
    }

    private fun ClassMirrorInterface.withMethod(kFunction: KFunction<*>): FunctionMirrorInterface {
        return this.methods.first { it.functionName == kFunction.name }
    }

    private fun ClassMirrorInterface.withField(kProperty: KProperty<*>): FieldMirrorInterface {
        return this.fields.first { it.fieldName == kProperty.name }
    }

}