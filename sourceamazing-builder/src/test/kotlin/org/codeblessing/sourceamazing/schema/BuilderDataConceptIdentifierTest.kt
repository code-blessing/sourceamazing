package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DuplicateConceptIdentifierException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataConceptIdentifierTest {

    @Schema(concepts = [
        SchemaWithConcepts.ConceptOne::class,
        SchemaWithConcepts.ConceptTwo::class,
    ])
    private interface SchemaWithConcepts {

        @Concept(facets = [])
        interface ConceptOne

        @Concept(facets = [])
        interface ConceptTwo

        @QueryConcepts(conceptClasses = [ConceptOne::class, ConceptTwo::class ])
        fun getAllConcept(): List<Any>

    }


    @Builder
    private interface BuilderToAddConcepts {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConcepts.ConceptOne::class, declareConceptAlias = "myConcept")
        fun createConceptOne(
            @SetConceptIdentifierValue(conceptToModifyAlias = "myConcept")
            conceptIdentifier: ConceptIdentifier,
        )

        @BuilderMethod
        @NewConcept(concept = SchemaWithConcepts.ConceptTwo::class, declareConceptAlias = "myConcept")
        fun createConceptTwo(
            @SetConceptIdentifierValue(conceptToModifyAlias = "myConcept")
            conceptIdentifier: ConceptIdentifier,
        )
    }

    @Test
    fun `test using the different concept identifier for creating same and different concepts should not fail`() {
        val myConceptId1 = ConceptIdentifier.of("My-Id-1")
        val myConceptId2 = ConceptIdentifier.of("My-Id-2")
        val myConceptId3 = ConceptIdentifier.of("My-Id-3")

        val schemaInstance = SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConcepts::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderToAddConcepts::class) { builder ->
                builder.createConceptOne(myConceptId1)
                builder.createConceptOne(myConceptId2)
                builder.createConceptTwo(myConceptId3)
            }
        }

        assertEquals(3, schemaInstance.getAllConcept().size)
    }


    @Test
    fun `test using the same concept identifier for creating same concepts throws an exception`() {
        val myConceptId = ConceptIdentifier.of("My-Id")

        assertThrows<DuplicateConceptIdentifierException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConcepts::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddConcepts::class) { builder ->
                    builder.createConceptOne(myConceptId)
                    builder.createConceptOne(myConceptId)
                }
            }
        }
    }

    @Test
    fun `test using the same concept identifier for creating different concepts throws an exception`() {
        val myConceptId = ConceptIdentifier.of("My-Id")

        assertThrows<DuplicateConceptIdentifierException> {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConcepts::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddConcepts::class) { builder ->
                    builder.createConceptOne(myConceptId)
                    builder.createConceptTwo(myConceptId)
                }
            }
        }
    }

}