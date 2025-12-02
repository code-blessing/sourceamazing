package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.addFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DuplicateConceptIdentifierException
import org.codeblessing.sourceamazing.schema.api.newConceptData
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SchemaDataConceptIdentifierTest {

    private interface MyConcepts {

        interface AbstractNumericConcept

        interface ConceptOne : AbstractNumericConcept

        interface ConceptTwo : AbstractNumericConcept

        @References([ConceptOne::class, ConceptTwo::class]) val concepts: List<AbstractNumericConcept>
    }

    @Test
    fun `test using the different concept identifier for creating same and different concepts should not fail`() {
        val id1 = ConceptIdentifier.of("My-Id-1")
        val id2 = ConceptIdentifier.of("My-Id-2")
        val id3 = ConceptIdentifier.of("My-Id-3")

        val schemaInstance =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                schemaContext.withRootInstance<MyConcepts> { rootConcept ->
                    schemaContext.dataCollector.newConceptData<MyConcepts.ConceptOne>(id1)
                    schemaContext.dataCollector.newConceptData<MyConcepts.ConceptTwo>(id2)
                    schemaContext.dataCollector.newConceptData<MyConcepts.ConceptOne>(id3)

                    rootConcept.addFacetValue(MyConcepts::concepts, id1)
                    rootConcept.addFacetValue(MyConcepts::concepts, id2)
                    rootConcept.addFacetValue(MyConcepts::concepts, id3)
                }
            }

        assertEquals(3, schemaInstance.concepts.size)
    }

    @Test
    fun `test using the same concept identifier for creating same concepts throws an exception`() {
        val id = ConceptIdentifier.of("My-Id")
        assertThrows<DuplicateConceptIdentifierException> {
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                schemaContext.withRootInstance<MyConcepts> { rootConcept ->
                    schemaContext.dataCollector.newConceptData<MyConcepts.ConceptOne>(id)
                    schemaContext.dataCollector.newConceptData<MyConcepts.ConceptOne>(id)
                    rootConcept.addFacetValue(MyConcepts::concepts, id)
                }
            }
        }
    }

    @Test
    fun `test using the same concept identifier for creating different concepts throws an exception`() {
        val id = ConceptIdentifier.of("My-Id")
        assertThrows<DuplicateConceptIdentifierException> {
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                schemaContext.withRootInstance<MyConcepts> { rootConcept ->
                    schemaContext.dataCollector.newConceptData<MyConcepts.ConceptOne>(id)
                    schemaContext.dataCollector.newConceptData<MyConcepts.ConceptTwo>(id)
                    rootConcept.addFacetValue(MyConcepts::concepts, id)
                }
            }
        }
    }
}
