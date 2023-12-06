package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.FacetType
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaCreatorFacetPrimitiveTypeAnnotationTest {

    @Schema(concepts = [SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets::class])
    private interface SchemaWithConceptWithPrimitiveFacetClasses {
        @Concept(facets = [
            ConceptClassWithFacets.TextFacetClass::class,
            ConceptClassWithFacets.BooleanFacetClass::class,
            ConceptClassWithFacets.NumberFacetClass::class,
        ])
        interface ConceptClassWithFacets {
            @Facet(FacetType.TEXT)
            interface TextFacetClass
            @Facet(FacetType.BOOLEAN)
            interface BooleanFacetClass
            @Facet(FacetType.NUMBER)
            interface NumberFacetClass
        }
    }

    @Test
    fun `test concept having three primitive type facet`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithConceptWithPrimitiveFacetClasses::class)
        assertEquals(1, schema.numberOfConcepts())
        val conceptSchema = schema.conceptByConceptName(ConceptName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets::class))
        assertEquals(3, conceptSchema.facets.size)

        val textFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets.TextFacetClass::class)
        val booleanFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets.BooleanFacetClass::class)
        val numberFacetName = FacetName.of(SchemaWithConceptWithPrimitiveFacetClasses.ConceptClassWithFacets.NumberFacetClass::class)

        assertEquals(textFacetName, conceptSchema.facets[0].facetName)
        assertEquals(booleanFacetName, conceptSchema.facets[1].facetName)
        assertEquals(numberFacetName, conceptSchema.facets[2].facetName)

        assertEquals(FacetType.TEXT, conceptSchema.facets[0].facetType)
        assertEquals(FacetType.BOOLEAN, conceptSchema.facets[1].facetType)
        assertEquals(FacetType.NUMBER, conceptSchema.facets[2].facetType)

        assertEquals(Unit::class, conceptSchema.facets[0].enumerationType)
        assertEquals(Unit::class, conceptSchema.facets[1].enumerationType)
        assertEquals(Unit::class, conceptSchema.facets[2].enumerationType)

        assertEquals(0, conceptSchema.facets[0].enumerationValues().size)
        assertEquals(0, conceptSchema.facets[1].enumerationValues().size)
        assertEquals(0, conceptSchema.facets[2].enumerationValues().size)

    }
}