package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.reflect.KClass

class TypePlaygroundTest {

    @Test
    fun `test with simple annotation`() {
        val annotation = StringFacet()
        assertNotNull(annotation)
    }

    @Test
    fun `test with simple annotation parametrized with primitives`() {
        val annotation = StringFacet(minimumOccurrences = 1, maximumOccurrences = 42)
        assertNotNull(annotation)
    }

    enum class MyEnum {
        FOO, BAR
    }

    @Test
    fun `test with annotation having class type`() {


        val annotation = EnumFacet(enumerationClass = MyEnum::class, minimumOccurrences = 1, maximumOccurrences = 2)
        assertNotNull(annotation)
    }

    @Test
    fun `test with annotation having fake class type`() {
        val myEnum = FakeKClass.enumMirror(className = "MyEnum", packageName = "foo.bar", "FOO", "BAR")

        val annotation = EnumFacet(enumerationClass = myEnum, minimumOccurrences = 1, maximumOccurrences = 2)
        assertNotNull(annotation)
    }


    data class MyFakeEnumFacet(
        val enumerationClass: KClass<*>,
        val minimumOccurrences: Int,
        val maximumOccurrences: Int,
    ): Annotation {


    }

    @Test
    fun `test with annotation having fake annotations`() {
        val myEnum = FakeKClass.enumMirror(className = "MyEnum", packageName = "foo.bar", "FOO", "BAR")
        val myFakeEnumFacetAnnotation = MyFakeEnumFacet(
            enumerationClass = myEnum, minimumOccurrences = 1, maximumOccurrences = 2
        )
        val myClassWithFakeAnnotation = FakeKClass.classMirror("myClass").withAnnotation(myFakeEnumFacetAnnotation)
        assertNotNull(myClassWithFakeAnnotation)
    }

}