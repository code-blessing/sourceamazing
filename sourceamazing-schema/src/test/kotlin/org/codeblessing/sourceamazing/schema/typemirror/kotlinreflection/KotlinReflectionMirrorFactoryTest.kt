package org.codeblessing.sourceamazing.schema.typemirror.kotlinreflection

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinReflectionMirrorFactoryTest {

    @Schema(concepts = [])
    @Concept(facets = [])
    @StringFacet
    private interface InterfaceWithAnnotationAndMethods

    @Test
    fun convertToTypeMirror() {
        val interfaceMirror = KotlinReflectionMirrorFactory.convertToMirrorHierarchy(InterfaceWithAnnotationAndMethods::class)
        assertEquals(2, interfaceMirror.annotations.size)
        assertEquals(3, interfaceMirror.methods.size)

    }
}