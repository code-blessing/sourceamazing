package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.datacollection.defaults.DefaultConceptDataCollector
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val personConceptName = "Person"
private const val personFirstnameFacetName = "Firstname"
private const val personAgeFacetName = "Age"
private const val skillConceptName = "Skill"
private const val skillDescriptionFacetName = "Description"
private const val skillStillEnjoyingFacetName = "StillEnjoying"

class DataCollectorInvocationHandlerTest {

    private val personConcept = ConceptName.of(personConceptName)
    private val personFirstnameFacet = FacetName.of(personFirstnameFacetName)
    private val personAgeFacet = FacetName.of(personAgeFacetName)
    private val skillConcept = ConceptName.of(skillConceptName)
    private val skillDescriptionFacet = FacetName.of(skillDescriptionFacetName)
    private val skillStillEnjoyingFacet = FacetName.of(skillStillEnjoyingFacetName)

    private val jamesConceptIdentifier = ConceptIdentifier.of("James")
    private val cookingConceptIdentifier = ConceptIdentifier.of("Cooking")
    private val skateboardConceptIdentifier = ConceptIdentifier.of("Skateboard")
    private val lindaConceptIdentifier = ConceptIdentifier.of("Linda")
    private val judoConceptIdentifier = ConceptIdentifier.of("Judo")


    @Test
    fun `test data invocation with default data collector`() {
        val conceptDataCollector = createDataCollector()
        val dataCollectorProxy: DefaultConceptDataCollector = ProxyCreator.createProxy(DefaultConceptDataCollector::class.java, DataCollectorInvocationHandler(conceptDataCollector))

        // Add data in builder style
        val james = dataCollectorProxy
            .newConceptData(
                conceptName = personConcept,
                conceptIdentifier = jamesConceptIdentifier,
            )
            .addFacetValue(facetName = personFirstnameFacet, facetValue = "James")
            .addFacetValue(facetName = personAgeFacet, facetValue = 18)
        james.newConceptData(
            conceptName = skillConcept,
            conceptIdentifier = cookingConceptIdentifier,
        )
            .addFacetValue(facetName = skillDescriptionFacet, facetValue = "Cooking for Dinner")
            .addFacetValue(facetName = skillStillEnjoyingFacet, facetValue = true)
        james.newConceptData(
            conceptName = skillConcept,
            conceptIdentifier = skateboardConceptIdentifier,
        )
            .addFacetValue(facetName = skillDescriptionFacet, facetValue = "Skateboarding")
            .addFacetValue(facetName = skillStillEnjoyingFacet, facetValue = false)

        // Add data in DSL style
        dataCollectorProxy
            .newConceptData(
                conceptName = personConcept,
                conceptIdentifier = lindaConceptIdentifier,
            ) {
                addFacetValue(facetName = personFirstnameFacet, facetValue = "Linda")
                addFacetValue(facetName = personAgeFacet, facetValue = 29)

                newConceptData(
                    conceptName = skillConcept,
                    conceptIdentifier = judoConceptIdentifier,
                ) {
                    addFacetValue(facetName = skillDescriptionFacet, facetValue = "Judo")
                    addFacetValue(facetName = skillStillEnjoyingFacet, facetValue = true)
                }
            }

        checkAssertions(conceptDataCollector)
    }


    @DataCollector
    interface DataCollectorRoot {

        // Builder style
        @AddConceptAndFacets(conceptBuilderClazz = PersonConceptBuilder::class)
        fun newPerson(
            @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ParameterDefinedConceptName conceptName: ConceptName = ConceptName.of(personConceptName),
            @FacetValue(personFirstnameFacetName) firstname: String,
        ): PersonConceptBuilder

        // DSL style
        @AddConceptAndFacets(conceptBuilderClazz = PersonConceptBuilder::class)
        fun newPerson(
            @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ParameterDefinedConceptName conceptName: ConceptName = ConceptName.of(personConceptName),
            @ConceptBuilder builder: PersonConceptBuilder.() -> Unit,
        )
    }

    @DataCollector
    interface PersonConceptBuilder {

        @AddFacets
        fun firstname(
            @ValueOfParameterDefinedFacetName firstname: String,
            @ParameterDefinedFacetName facetName: FacetName = FacetName.of(personFirstnameFacetName),
        ): PersonConceptBuilder

        @AddFacets
        fun age(
            @FacetValue(personAgeFacetName) age: Int,
        ): PersonConceptBuilder

        @AddFacets
        fun firstnameAndAge(
            @ValueOfParameterDefinedFacetName firstname: String,
            @ParameterDefinedFacetName facetName: FacetName = FacetName.of(personFirstnameFacetName),
            @FacetValue(personAgeFacetName) age: Int,
        ): PersonConceptBuilder

        // Builder style
        @AddConceptAndFacets(conceptBuilderClazz = SkillConceptBuilder::class)
        @ConceptNameValue(skillConceptName)
        @AutoRandomConceptIdentifier
        fun skill(
            @ConceptIdentifierValue skillConceptIdentifier: ConceptIdentifier? = null,
        ): SkillConceptBuilder

        // DSL style
        @AddConceptAndFacets(conceptBuilderClazz = SkillConceptBuilder::class)
        @ConceptNameValue(skillConceptName)
        fun skill(
            @ConceptIdentifierValue skillConceptIdentifier: String?,
            @ConceptBuilder builder: SkillConceptBuilder.() -> Unit)

    }

    @DataCollector
    interface SkillConceptBuilder {

        @AddFacets
        fun descriptionAndStillEnjoying(
            @FacetValue(skillDescriptionFacetName) description: String,
            @FacetValue(skillStillEnjoyingFacetName) stillEnjoying: Boolean,
        ): SkillConceptBuilder

        @AddFacets
        fun description(
            @FacetValue(skillDescriptionFacetName) description: String,
        ): SkillConceptBuilder

        @AddFacets
        fun stillEnjoying(
            @FacetValue(skillStillEnjoyingFacetName) stillEnjoying: Boolean,
        ): SkillConceptBuilder

    }


    @Test
    fun `test data invocation with individual data collector`() {
        val conceptDataCollector = createDataCollector()
        val dataCollectorProxy: DataCollectorRoot = ProxyCreator.createProxy(DataCollectorRoot::class.java, DataCollectorInvocationHandler(conceptDataCollector))

        // Add data in builder style
        val james = dataCollectorProxy
            .newPerson(jamesConceptIdentifier, firstname = "James")
            .age(18)
        james.skill(cookingConceptIdentifier)
            .descriptionAndStillEnjoying("Cooking for Dinner", true)
        james.skill(skateboardConceptIdentifier)
            .description("Skateboarding")
            .stillEnjoying(false)

        // Add data in DSL style
        val lindaConceptIdentifier = ConceptIdentifier.of("Linda")
        val judoConceptIdentifier = ConceptIdentifier.of("Judo")
        dataCollectorProxy
            .newPerson(lindaConceptIdentifier) {
                firstnameAndAge(firstname = "Linda", age = 29)
                skill(judoConceptIdentifier.name) {
                    description("Judo")
                    stillEnjoying(true)
                }
            }

        checkAssertions(conceptDataCollector)
    }

    private fun checkAssertions(conceptDataCollector: ConceptDataCollector) {
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

    private fun createDataCollector(): ConceptDataCollector {
        return ConceptDataCollector()
    }

}