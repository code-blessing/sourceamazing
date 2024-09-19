package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateConceptMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.EnumFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.IntFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptsAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ReferenceFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory


class SchemaCreatorConceptAnnotationTest {

    @Test
    fun `test unannotated concept class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                // concept without concept annotation
            }
        }
        assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test concept class with two concept annotations should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                // concept with two concept annotation
                withAnnotationOnConcept(Concept(facets = emptyArray()),)
                withAnnotationOnConcept(Concept(facets = emptyArray()),)

            }
        }
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @TestFactory
    fun `test wrong class type for concept should throw an exception`(): List<DynamicTest> {

        data class TestData(
            val description: String,
            val conceptMirror: FakeKClass
        )

        return listOf(
            TestData(
                "concept with class instead of interface",
                FakeSchemaMirrorDsl.concept {
                    setConceptIsClass()
                }
            ),
            TestData(
                "concept with enum instead of interface",
                FakeSchemaMirrorDsl.concept {
                    conceptMirror = conceptMirror.setIsEnum()
                    conceptMirror = conceptMirror.setEnumValues("Foo", "Bar")
                }
            ),
            TestData(
                "concept with object class instead of interface",
                FakeSchemaMirrorDsl.concept {
                    conceptMirror = conceptMirror.setIsObjectClass()
                }
            ),
            TestData(
                "concept with data class instead of interface",
                FakeSchemaMirrorDsl.concept {
                    conceptMirror = conceptMirror.setIsDataClass()
                }
            ),
            TestData(
                "concept with annotation class instead of interface",
                FakeSchemaMirrorDsl.concept {
                    conceptMirror = conceptMirror.setIsAnnotation()
                }
            ),
        ).map { (description, conceptMirror) ->
            DynamicTest.dynamicTest(description) {
                val schemaMirror = FakeSchemaMirrorDsl.schema {
                    concept(conceptMirror)
                }
                assertThrows(NotInterfaceMalformedSchemaException::class.java) {
                    SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
                }
            }
        }
    }

    @TestFactory
    fun `test wrong annotations on concept should throw an exception`(): List<DynamicTest> {
        return listOf(
            Schema(emptyArray()),
            BooleanFacet(),
            IntFacet(),
            StringFacet(),
            EnumFacet(CommonFakeMirrors.enumClassMirror("FOO", "BAR")),
            ReferenceFacet(emptyArray()),
            QueryConceptIdentifierValue(),
            QueryConcepts(emptyArray()),
            QueryFacetValue(CommonFakeMirrors.anyClassMirror()),
        ).map { annotationMirror ->
            DynamicTest.dynamicTest(
                "Annotation ${annotationMirror.annotationClass} not allowed on concept interface.") {
                val schemaMirror = FakeSchemaMirrorDsl.schema {
                    concept {
                        withAnnotationOnConcept(annotationMirror)
                    }
                }
                assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
                    SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
                }
            }
        }
    }

    @Test
    fun `test duplicate concept classes should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema(addSchemaAnnotationWithAllConcepts = false) {
            val conceptClassMirror = concept {
                // a concept
            }
            withAnnotationOnSchema(Schema(arrayOf(conceptClassMirror, conceptClassMirror)))
        }
        assertThrows(DuplicateConceptMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    @Disabled("Not prevented currently")
    fun `test concept with two concept annotations in hierarchy should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            val parentConcept = concept {
                // parentConcept
            }
            concept {
                // childConcept

                withSuperClassMirror(parentConcept)
            }
        }

        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test create an schema with an empty concept class should succeed`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                // empty concept class
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        assertEquals(1, schema.numberOfConcepts())
    }
}