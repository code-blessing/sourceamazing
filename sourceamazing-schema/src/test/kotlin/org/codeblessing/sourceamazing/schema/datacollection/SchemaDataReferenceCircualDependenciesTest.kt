package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.addFacetValue
import org.codeblessing.sourceamazing.schema.api.datacollection.newConceptData
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaDataReferenceCircualDependenciesTest {

    private interface MyConcepts {

        interface MyConcept {
            val id: String

            val conceptReferences: List<MyConcept>
        }

        val concepts: List<MyConcept>
    }

    @Test
    fun `test add reference to itself`() {
        val selfReferencingId = ConceptIdentifier.of("Self-Referencing-Id")
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                schemaContext.withRootInstance<MyConcepts> { root ->
                    val selfReferencingConcept =
                        schemaContext.dataCollector
                            .newConceptData<MyConcepts.MyConcept>(selfReferencingId)
                            .addFacetValue(MyConcepts.MyConcept::id, "self-referenced")

                    selfReferencingConcept.addFacetValue(
                        MyConcepts.MyConcept::conceptReferences,
                        selfReferencingConcept.conceptIdentifier,
                    )

                    root.addFacetValue(MyConcepts::concepts, selfReferencingConcept.conceptIdentifier)
                }
            }

        val selfReferencingConcept = schemaInstance.concepts.first()
        assertEquals("self-referenced", selfReferencingConcept.id)
        assertEquals("self-referenced", selfReferencingConcept.conceptReferences.first().id)
    }

    @Test
    fun `test three concept referencing each other in a circle`() {
        val firstId = ConceptIdentifier.of("First-Referenced-Id")
        val secondId = ConceptIdentifier.of("Second-Referenced-Id")
        val thirdId = ConceptIdentifier.of("Third-Referenced-Id")

        val schemaInstance: MyConcepts =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                schemaContext.withRootInstance<MyConcepts> { root ->
                    val firstConcept =
                        schemaContext.dataCollector
                            .newConceptData<MyConcepts.MyConcept>(firstId)
                            .addFacetValue(MyConcepts.MyConcept::id, "first")

                    val secondConcept =
                        schemaContext.dataCollector
                            .newConceptData<MyConcepts.MyConcept>(secondId)
                            .addFacetValue(MyConcepts.MyConcept::id, "second")

                    val thirdConcept =
                        schemaContext.dataCollector
                            .newConceptData<MyConcepts.MyConcept>(thirdId)
                            .addFacetValue(MyConcepts.MyConcept::id, "third")

                    firstConcept.addFacetValue(MyConcepts.MyConcept::conceptReferences, secondConcept.conceptIdentifier)
                    secondConcept.addFacetValue(MyConcepts.MyConcept::conceptReferences, thirdConcept.conceptIdentifier)
                    thirdConcept.addFacetValue(MyConcepts.MyConcept::conceptReferences, firstConcept.conceptIdentifier)

                    root.addFacetValue(MyConcepts::concepts, firstConcept.conceptIdentifier)
                }
            }

        val theFirst = schemaInstance.concepts.first()
        assertEquals("first", theFirst.id)
        val theSecond = theFirst.conceptReferences.first()
        assertEquals("second", theSecond.id)
        val theThird = theSecond.conceptReferences.first()
        assertEquals("third", theThird.id)
        val theFirstAgain = theThird.conceptReferences.first()
        assertEquals("first", theFirstAgain.id)
    }
}
