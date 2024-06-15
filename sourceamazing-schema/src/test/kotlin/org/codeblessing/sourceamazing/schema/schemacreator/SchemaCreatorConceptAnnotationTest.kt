package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.DuplicateConceptMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.NotInterfaceMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.BooleanFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
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
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test concept class with two concept annotations should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                // concept with two concept annotation
                withAnnotationOnConcept(ConceptAnnotationMirror(emptyList()),)
                withAnnotationOnConcept(ConceptAnnotationMirror(emptyList()),)

            }
        }
        assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @TestFactory
    fun `test wrong class type for concept should throw an exception`(): List<DynamicTest> {

        data class TestData(
            val description: String,
            val conceptMirror: FakeClassMirror
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
                    SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
                }
            }
        }
    }

    @TestFactory
    fun `test wrong annotations on concept should throw an exception`(): List<DynamicTest> {
        return listOf(
            SchemaAnnotationMirror(emptyList()),
            BooleanFacetAnnotationMirror(),
            IntFacetAnnotationMirror(),
            StringFacetAnnotationMirror(),
            EnumFacetAnnotationMirror(CommonFakeMirrors.enumClassMirror("FOO", "BAR")),
            ReferenceFacetAnnotationMirror(emptyList()),
            QueryConceptIdentifierValueAnnotationMirror(),
            QueryConceptsAnnotationMirror(emptyList()),
            QueryFacetValueAnnotationMirror(CommonFakeMirrors.anyClassMirror()),
        ).map { annotationMirror ->
            DynamicTest.dynamicTest(
                "Annotation ${annotationMirror.annotationClass} not allowed on concept interface.") {
                val schemaMirror = FakeSchemaMirrorDsl.schema {
                    concept {
                        withAnnotationOnConcept(annotationMirror)
                    }
                }
                assertThrows(WrongAnnotationMalformedSchemaException::class.java) {
                    SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
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
            withAnnotationOnSchema(SchemaAnnotationMirror(listOf(conceptClassMirror, conceptClassMirror)))
        }
        assertThrows(DuplicateConceptMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
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
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test create an schema with an empty concept class should succeed`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                // empty concept class
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        assertEquals(1, schema.numberOfConcepts())
    }
}