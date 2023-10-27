package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.defaults.DefaultConceptDataCollector
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DataCollectorInvocationHandlerTest {


    @Test
    fun `test data invocation with default data collector`() {
        val conceptDataCollector = ConceptDataCollector(TestSchema(emptyList()), validateConcept = false)
        val dataCollectorProxy: DefaultConceptDataCollector = ProxyCreator.createProxy(DefaultConceptDataCollector::class.java, DataCollectorInvocationHandler(conceptDataCollector))

        val personConcept = ConceptName.of("Person")
        val personFirstnameFacet = FacetName.of("Firstname")
        val personAgeFacet = FacetName.of("Age")

        val skillConcept = ConceptName.of("Skill")
        val skillDescriptionFacet = FacetName.of("Description")


        val jamesConceptIdentifier = ConceptIdentifier.of("James")
        val cookingConceptIdentifier = ConceptIdentifier.of("Cooking")
        val skateboardConceptIdentifier = ConceptIdentifier.of("Skateboard")
        val james = dataCollectorProxy
            .newConceptData(
                conceptName = personConcept,
                conceptIdentifier = jamesConceptIdentifier,
                parentConceptIdentifier = null
            )
            .addFacetValue(facetName = personFirstnameFacet, facetValue = "James")
            .addFacetValue(facetName = personAgeFacet, facetValue = 18)
        james.newConceptData(
            conceptName = skillConcept,
            conceptIdentifier = cookingConceptIdentifier,
            parentConceptIdentifier = jamesConceptIdentifier
        ).addFacetValue(facetName = skillDescriptionFacet, facetValue = "Cooking for Dinner")
        james.newConceptData(
            conceptName = skillConcept,
            conceptIdentifier = skateboardConceptIdentifier,
            parentConceptIdentifier = jamesConceptIdentifier
        ).addFacetValue(facetName = skillDescriptionFacet, facetValue = "Skateboarding")


        val lindaConceptIdentifier = ConceptIdentifier.of("Linda")
        val judoConceptIdentifier = ConceptIdentifier.of("Judo")
        val linda = dataCollectorProxy
            .newConceptData(
                conceptName = personConcept,
                conceptIdentifier = lindaConceptIdentifier,
                parentConceptIdentifier = null
            )
            .addFacetValue(facetName = personFirstnameFacet, facetValue = "Linda")
            .addFacetValue(facetName = personAgeFacet, facetValue = 29)
        linda.newConceptData(
            conceptName = skillConcept,
            conceptIdentifier = judoConceptIdentifier,
            parentConceptIdentifier = lindaConceptIdentifier
        ).addFacetValue(facetName = skillDescriptionFacet, facetValue = "Judo")


        val conceptDataList = conceptDataCollector.provideConceptData()
        assertEquals(5, conceptDataList.size)

        assertEquals("Linda", conceptDataList
            .single { it.conceptIdentifier == lindaConceptIdentifier}
            .getFacet(personFirstnameFacet)
        )
        assertEquals(18, conceptDataList
            .single { it.conceptIdentifier == jamesConceptIdentifier}
            .getFacet(personAgeFacet)
        )

        assertEquals("Judo", conceptDataList
            .single { it.conceptIdentifier == judoConceptIdentifier}
            .getFacet(skillDescriptionFacet)
        )
        assertEquals(lindaConceptIdentifier, conceptDataList
            .single { it.conceptIdentifier == judoConceptIdentifier}
            .parentConceptIdentifier
        )

    }

}