package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class JavaReflectionMirrorFactoryTest {

    @Schema(concepts = [MyConceptInterface::class])
    private interface MySchemaInterface

    @Concept(facets = [MyFacetInterface::class])
    private interface MyConceptInterface

    @StringFacet
    private interface MyFacetInterface {
        fun giveMeTheConcept(): MyConceptInterface
    }


    @Test
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
        require(againConceptInterface is ClassMirror)
        assertEquals(conceptInterfaceMirror.classQualifier, againConceptInterface.classQualifier)
        assertFalse(conceptInterfaceMirror.classQualifier === againConceptInterface.classQualifier)
    }

    private interface AnInterface

    @Test
    fun `represents the interface default methods`() {
        val myInterfaceMirror = JavaReflectionMirrorFactory.convertToMirrorHierarchy(AnInterface::class)
        assertEquals(0, myInterfaceMirror.annotations.size)
        assertEquals(3, myInterfaceMirror.methods.size)

        val toStringMethod = myInterfaceMirror.methods.first { it.functionName == "toString" }
        assertEquals("toString", toStringMethod.functionName)
        assertEquals(0, toStringMethod.parameters.size)
        val returnType = requireNotNull(toStringMethod.returnType)
        assertEquals(false, returnType.type.nullable)
        val returnTypeClassMirror = returnType.type.signatureMirror.provideMirror() as ClassMirror
        assertEquals("kotlin.String", returnTypeClassMirror.fullQualifiedName)
    }
}