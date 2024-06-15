package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongTypeMalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.ReferenceFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaCreatorFacetReferenceTypeAnnotationTest {

    @Test
    fun `test concept having an empty reference facet should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(ReferenceFacetAnnotationMirror(emptyList()))
                }
            }
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test concept having a reference facet referencing one concept`() {
        lateinit var conceptClassWithReferenceFacet: FakeClassMirror
        lateinit var myReferenceToOtherConceptFacet: FakeClassMirror
        lateinit var otherConcept: FakeClassMirror

        val schemaMirror = FakeSchemaMirrorDsl.schema {
            otherConcept = concept {
                // no facets
            }
            conceptClassWithReferenceFacet = concept {
                myReferenceToOtherConceptFacet = facet {
                    withAnnotationOnFacet(ReferenceFacetAnnotationMirror(listOf(otherConcept)))
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(conceptClassWithReferenceFacet))
        val referenceFacetName = FacetName.of(myReferenceToOtherConceptFacet)
        val otherReferencedConceptName = ConceptName.of(otherConcept)
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName)
        assertEquals(referenceFacetName, referenceFacetSchema.facetName)
        assertEquals(FacetType.REFERENCE, referenceFacetSchema.facetType)
        assertEquals(1, referenceFacetSchema.referencingConcepts.size)
        assertEquals(otherReferencedConceptName, referenceFacetSchema.referencingConcepts.first())
    }

    @Test
    fun `test concept having a reference facet referencing multiple concept`() {
        lateinit var conceptClassWithReferenceFacet: FakeClassMirror
        lateinit var myReferenceToOtherConceptFacet: FakeClassMirror
        lateinit var otherConcept: FakeClassMirror
        lateinit var andAnotherConcept: FakeClassMirror
        lateinit var andJustOneAnotherConcept: FakeClassMirror

        val schemaMirror = FakeSchemaMirrorDsl.schema {
            otherConcept = concept {
                // no facets
            }
            andAnotherConcept = concept {
                // no facets
            }
            andJustOneAnotherConcept = concept {
                // no facets
            }
            conceptClassWithReferenceFacet = concept {
                myReferenceToOtherConceptFacet = facet {
                    withAnnotationOnFacet(ReferenceFacetAnnotationMirror(
                        listOf(otherConcept, andAnotherConcept, andJustOneAnotherConcept)
                    ))
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)

        val conceptSchema = schema.conceptByConceptName(ConceptName.of(conceptClassWithReferenceFacet))
        val referenceFacetName = FacetName.of(myReferenceToOtherConceptFacet)
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName)

        val referencedConceptName1 = ConceptName.of(otherConcept)
        val referencedConceptName2 = ConceptName.of(andAnotherConcept)
        val referencedConceptName3 = ConceptName.of(andJustOneAnotherConcept)

        assertEquals(referenceFacetName, referenceFacetSchema.facetName)
        assertEquals(FacetType.REFERENCE, referenceFacetSchema.facetType)
        assertEquals(3, referenceFacetSchema.referencingConcepts.size)
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName1))
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName2))
        assertTrue(referenceFacetSchema.referencingConcepts.contains(referencedConceptName3))
    }

    @Test
    fun `test reference facet to unknown concept should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema(addSchemaAnnotationWithAllConcepts = false) {
            val unknownConcept = concept {
                // concept not listed on schema
            }
            val knownConceptWithReference = concept {
                facet {
                    withAnnotationOnFacet(ReferenceFacetAnnotationMirror(listOf(unknownConcept)))
                }
            }
            withAnnotationOnSchema(SchemaAnnotationMirror(listOf(knownConceptWithReference)))
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test reference facet to a non-concept class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            val conceptWithoutConceptAnnotation = concept(addConceptAnnotationWithAllFacets = false) {
                // concept not a concept
            }
            concept {
                facet {
                    withAnnotationOnFacet(ReferenceFacetAnnotationMirror(listOf(conceptWithoutConceptAnnotation)))
                }
            }
        }


        Assertions.assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }
}