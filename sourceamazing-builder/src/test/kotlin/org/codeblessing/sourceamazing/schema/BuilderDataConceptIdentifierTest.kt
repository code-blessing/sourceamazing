package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DuplicateConceptIdentifierException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataConceptIdentifierTest {

    private interface SchemaWithConcepts {

        interface AbstractNumericConcept

        interface ConceptOne: AbstractNumericConcept

        interface ConceptTwo: AbstractNumericConcept

        @References([ConceptOne::class, ConceptTwo::class])
        val concepts: List<AbstractNumericConcept>

    }


    @Builder
    @ExpectedAliasFromSuperiorBuilder("root")
    private interface BuilderToAddConcepts {

        @BuilderMethod
        @NewConcept(concept = SchemaWithConcepts.ConceptOne::class, declareConceptAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "myConcept")
        fun createConceptOne(
            @SetConceptIdentifierValue(conceptToModifyAlias = "myConcept")
            conceptIdentifier: ConceptIdentifier,
        )

        @BuilderMethod
        @NewConcept(concept = SchemaWithConcepts.ConceptTwo::class, declareConceptAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "myConcept")
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

        val schemaInstance = SchemaApi.withSchema(SchemaWithConcepts::class) { schemaContext ->
            withRootInstance<SchemaWithConcepts>(schemaContext) { conceptNameAndIdentifier ->  
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderToAddConcepts::class,
                    mapOf("root" to conceptNameAndIdentifier),
                ) { builder ->
                    builder.createConceptOne(myConceptId1)
                    builder.createConceptOne(myConceptId2)
                    builder.createConceptTwo(myConceptId3)
                }
            }
        }

        assertEquals(3, schemaInstance.concepts.size)
    }


    @Test
    fun `test using the same concept identifier for creating same concepts throws an exception`() {
        val myConceptId = ConceptIdentifier.of("My-Id")
        assertThrows<DuplicateConceptIdentifierException> {
            SchemaApi.withSchema(SchemaWithConcepts::class) { schemaContext ->
                withRootInstance<SchemaWithConcepts>(schemaContext) { conceptNameAndIdentifier ->  
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddConcepts::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConceptOne(myConceptId)
                        builder.createConceptOne(myConceptId)
                    }
                }
            }
        }
    }

    @Test
    fun `test using the same concept identifier for creating different concepts throws an exception`() {
        val myConceptId = ConceptIdentifier.of("My-Id")

        assertThrows<DuplicateConceptIdentifierException> {
            SchemaApi.withSchema(SchemaWithConcepts::class) { schemaContext ->
                withRootInstance<SchemaWithConcepts>(schemaContext) { conceptNameAndIdentifier ->  
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddConcepts::class,
                        mapOf("root" to conceptNameAndIdentifier),
                    ) { builder ->
                        builder.createConceptOne(myConceptId)
                        builder.createConceptTwo(myConceptId)
                    }
                }
            }
        }
    }

    @Test
    fun `test quick validation throws the exception immediately on the wrong builder method`() {
        val myConceptId = ConceptIdentifier.of("My-Id")

        SchemaApi.withSchema(SchemaWithConcepts::class) { schemaContext ->
            withRootInstance<SchemaWithConcepts>(schemaContext) { conceptNameAndIdentifier ->  
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderToAddConcepts::class,
                    mapOf("root" to conceptNameAndIdentifier),
                ) { builder ->
                    builder.createConceptOne(myConceptId)
                    assertThrows<DuplicateConceptIdentifierException> {
                        builder.createConceptTwo(myConceptId)
                    }
                }
            }
        }
    }

}
