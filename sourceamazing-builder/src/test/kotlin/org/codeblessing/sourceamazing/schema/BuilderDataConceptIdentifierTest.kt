package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DuplicateConceptIdentifierException
import org.codeblessing.sourceamazing.schema.api.datacollection.toConceptNameAndIdentifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuilderDataConceptIdentifierTest {

    private interface MyConcepts {

        interface AbstractNumericConcept

        interface ConceptOne : AbstractNumericConcept

        interface ConceptTwo : AbstractNumericConcept

        @References([ConceptOne::class, ConceptTwo::class]) val concepts: List<AbstractNumericConcept>
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderToAddConcepts {

        @BuilderMethod
        @NewConcept(concept = MyConcepts.ConceptOne::class, declareConceptAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConceptOne(
            @SetConceptIdentifierValue(conceptToModifyAlias = "myConcept") conceptIdentifier: ConceptIdentifier
        )

        @BuilderMethod
        @NewConcept(concept = MyConcepts.ConceptTwo::class, declareConceptAlias = "myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConceptTwo(
            @SetConceptIdentifierValue(conceptToModifyAlias = "myConcept") conceptIdentifier: ConceptIdentifier
        )
    }

    @Test
    fun `test using the different concept identifier for creating same and different concepts should not fail`() {
        val myConceptId1 = ConceptIdentifier.of("My-Id-1")
        val myConceptId2 = ConceptIdentifier.of("My-Id-2")
        val myConceptId3 = ConceptIdentifier.of("My-Id-3")

        val schemaInstance =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<MyConcepts> { conceptData ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddConcepts::class,
                        mapOf("root" to conceptData.toConceptNameAndIdentifier()),
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
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<MyConcepts> { conceptData ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddConcepts::class,
                        mapOf("root" to conceptData.toConceptNameAndIdentifier()),
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
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<MyConcepts> { conceptData ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderToAddConcepts::class,
                        mapOf("root" to conceptData.toConceptNameAndIdentifier()),
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

        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            schemaContext.withDefaultValueRootInstance<MyConcepts> { conceptData ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderToAddConcepts::class,
                    mapOf("root" to conceptData.toConceptNameAndIdentifier()),
                ) { builder ->
                    builder.createConceptOne(myConceptId)
                    assertThrows<DuplicateConceptIdentifierException> { builder.createConceptTwo(myConceptId) }
                }
            }
        }
    }
}
