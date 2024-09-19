package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MissingAnnotationMalformedSchemaException
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.WrongTypeMalformedSchemaException
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.toFacetName
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
                    withAnnotationOnFacet(ReferenceFacet(emptyArray()))
                }
            }
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test concept having a reference facet referencing one concept`() {
        lateinit var conceptClassWithReferenceFacet: FakeKClass
        lateinit var myReferenceToOtherConceptFacet: FakeKClass
        lateinit var otherConcept: FakeKClass

        val schemaMirror = FakeSchemaMirrorDsl.schema {
            otherConcept = concept {
                // no facets
            }
            conceptClassWithReferenceFacet = concept {
                myReferenceToOtherConceptFacet = facet {
                    withAnnotationOnFacet(ReferenceFacet(arrayOf(otherConcept)))
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)

        val conceptSchema = schema.conceptByConceptName(conceptClassWithReferenceFacet.toConceptName())
        val referenceFacetName = myReferenceToOtherConceptFacet.toFacetName()
        val otherReferencedConceptName = otherConcept.toConceptName()
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName)
        assertEquals(referenceFacetName, referenceFacetSchema.facetName)
        assertEquals(FacetType.REFERENCE, referenceFacetSchema.facetType)
        assertEquals(1, referenceFacetSchema.referencingConcepts.size)
        assertEquals(otherReferencedConceptName, referenceFacetSchema.referencingConcepts.first())
    }

    @Test
    fun `test concept having a reference facet referencing multiple concept`() {
        lateinit var conceptClassWithReferenceFacet: FakeKClass
        lateinit var myReferenceToOtherConceptFacet: FakeKClass
        lateinit var otherConcept: FakeKClass
        lateinit var andAnotherConcept: FakeKClass
        lateinit var andJustOneAnotherConcept: FakeKClass

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
                    withAnnotationOnFacet(ReferenceFacet(
                        arrayOf(otherConcept, andAnotherConcept, andJustOneAnotherConcept)
                    ))
                }
            }
        }

        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)

        val conceptSchema = schema.conceptByConceptName(conceptClassWithReferenceFacet.toConceptName())
        val referenceFacetName = myReferenceToOtherConceptFacet.toFacetName()
        val referenceFacetSchema = conceptSchema.facetByName(referenceFacetName)

        val referencedConceptName1 = otherConcept.toConceptName()
        val referencedConceptName2 = andAnotherConcept.toConceptName()
        val referencedConceptName3 = andJustOneAnotherConcept.toConceptName()

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
                    withAnnotationOnFacet(ReferenceFacet(arrayOf(unknownConcept)))
                }
            }
            withAnnotationOnSchema(Schema(arrayOf(knownConceptWithReference)))
        }

        Assertions.assertThrows(WrongTypeMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
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
                    withAnnotationOnFacet(ReferenceFacet(arrayOf(conceptWithoutConceptAnnotation)))
                }
            }
        }


        Assertions.assertThrows(MissingAnnotationMalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }
}