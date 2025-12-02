package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.annotations.References
import org.codeblessing.sourceamazing.schema.datacollection.SchemaDataAddOrReplaceValuesTest.MyConcepts.ConceptAlpha
import org.codeblessing.sourceamazing.schema.datacollection.SchemaDataAddOrReplaceValuesTest.MyConcepts.MyEnum.BAR
import org.codeblessing.sourceamazing.schema.datacollection.SchemaDataAddOrReplaceValuesTest.MyConcepts.MyEnum.FOO
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaDataAddOrReplaceValuesTest {
    private val alpha1Id = ConceptIdentifier.of("Alpha1-Id")
    private val alpha2Id = ConceptIdentifier.of("Alpha2-Id")
    private val beta1Id = ConceptIdentifier.of("Beta1-Id")

    private interface MyConcepts {

        enum class MyEnum {
            FOO,
            BAR,
        }

        interface AlphaAndBeta {
            val id: String
        }

        interface ConceptAlpha : AlphaAndBeta

        interface ConceptBeta : AlphaAndBeta

        val texts: List<String>

        val booleans: List<Boolean>

        val numbers: List<Int>

        val enumerations: List<MyEnum>

        @References([ConceptAlpha::class, ConceptBeta::class]) val references: List<AlphaAndBeta>
    }

    private fun addReferencedConcepts(schemaContext: SchemaContext) {
        schemaContext.dataCollector
            .newConceptData<ConceptAlpha>(alpha1Id)
            .addFacetValue(MyConcepts.AlphaAndBeta::id, alpha1Id.name)
        schemaContext.dataCollector
            .newConceptData<ConceptAlpha>(alpha2Id)
            .addFacetValue(MyConcepts.AlphaAndBeta::id, alpha2Id.name)
        schemaContext.dataCollector
            .newConceptData<ConceptAlpha>(beta1Id)
            .addFacetValue(MyConcepts.AlphaAndBeta::id, beta1Id.name)
    }

    private fun List<MyConcepts.AlphaAndBeta>.ids(): List<ConceptIdentifier> = this.map { it.id.toConceptIdentifier() }

    @Test
    fun `test insert to the same text facet multiple times with replaceFacetValues does always clear and override the result`() {
        val schemaInstance =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                addReferencedConcepts(schemaContext)

                schemaContext.withRootInstance<MyConcepts> { conceptData ->
                    conceptData.replaceFacetValues(MyConcepts::texts, listOf("hallo1"))
                    conceptData.replaceFacetValues(MyConcepts::texts, listOf("hallo2"))
                    conceptData.replaceFacetValues(MyConcepts::texts, listOf("hallo3"))

                    conceptData.replaceFacetValues(MyConcepts::booleans, listOf(true))
                    conceptData.replaceFacetValues(MyConcepts::booleans, listOf(false))
                    conceptData.replaceFacetValues(MyConcepts::booleans, listOf(true))

                    conceptData.replaceFacetValues(MyConcepts::numbers, listOf(1))
                    conceptData.replaceFacetValues(MyConcepts::numbers, listOf(2))
                    conceptData.replaceFacetValues(MyConcepts::numbers, listOf(3))

                    conceptData.replaceFacetValues(MyConcepts::enumerations, listOf(FOO))
                    conceptData.replaceFacetValues(MyConcepts::enumerations, listOf(BAR))
                    conceptData.replaceFacetValues(MyConcepts::enumerations, listOf(FOO))

                    conceptData.replaceFacetValues(MyConcepts::references, listOf(alpha1Id))
                    conceptData.replaceFacetValues(MyConcepts::references, listOf(beta1Id))
                    conceptData.replaceFacetValues(MyConcepts::references, listOf(alpha2Id))
                }
            }

        Assertions.assertEquals(listOf("hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(3), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha2Id), schemaInstance.references.ids())
    }

    @Test
    fun `test insert a list of strings to text facet with replaceFacetValues does replace with all list entries`() {
        val schemaInstance =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                addReferencedConcepts(schemaContext)

                schemaContext.withRootInstance<MyConcepts> { conceptData ->
                    conceptData.replaceFacetValues(MyConcepts::texts, listOf("hallo1", "hallo2"))
                    conceptData.replaceFacetValues(MyConcepts::texts, listOf("hallo2", "hallo3"))

                    conceptData.replaceFacetValues(MyConcepts::booleans, listOf(true, false))
                    conceptData.replaceFacetValues(MyConcepts::booleans, listOf(false, true))
                    conceptData.replaceFacetValues(MyConcepts::booleans, listOf(true, false))

                    conceptData.replaceFacetValues(MyConcepts::numbers, listOf(1, 2))
                    conceptData.replaceFacetValues(MyConcepts::numbers, listOf(2, 3))
                    conceptData.replaceFacetValues(MyConcepts::numbers, listOf(3, 4))

                    conceptData.replaceFacetValues(MyConcepts::enumerations, listOf(FOO, BAR))
                    conceptData.replaceFacetValues(MyConcepts::enumerations, listOf(BAR, FOO))
                    conceptData.replaceFacetValues(MyConcepts::enumerations, listOf(FOO, BAR))

                    conceptData.replaceFacetValues(MyConcepts::references, listOf(alpha1Id, beta1Id))
                    conceptData.replaceFacetValues(MyConcepts::references, listOf(beta1Id, alpha2Id))
                    conceptData.replaceFacetValues(MyConcepts::references, listOf(alpha2Id, beta1Id))
                }
            }

        Assertions.assertEquals(listOf("hallo2", "hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true, false), schemaInstance.booleans)
        Assertions.assertEquals(listOf(3, 4), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO, BAR), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha2Id, beta1Id), schemaInstance.references.ids())
    }

    @Test
    fun `test insert an empty list of strings to text facet with replaceFacetValues does replace with an empty list`() {
        val schemaInstance =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                addReferencedConcepts(schemaContext)

                schemaContext.withRootInstance<MyConcepts> { conceptData ->
                    conceptData.replaceFacetValues(MyConcepts::texts, listOf("hallo1", "hallo2"))
                    conceptData.replaceFacetValues(MyConcepts::texts, emptyList())

                    conceptData.replaceFacetValues(MyConcepts::booleans, listOf(true, false))
                    conceptData.replaceFacetValues(MyConcepts::booleans, emptyList())

                    conceptData.replaceFacetValues(MyConcepts::numbers, listOf(1, 2))
                    conceptData.replaceFacetValues(MyConcepts::numbers, emptyList())

                    conceptData.replaceFacetValues(MyConcepts::enumerations, listOf(FOO, BAR))
                    conceptData.replaceFacetValues(MyConcepts::enumerations, emptyList())

                    conceptData.replaceFacetValues(MyConcepts::references, listOf(alpha1Id, beta1Id))
                    conceptData.replaceFacetValues(MyConcepts::references, emptyList())
                }
            }

        Assertions.assertEquals(emptyList<String>(), schemaInstance.texts)
        Assertions.assertEquals(emptyList<Boolean>(), schemaInstance.booleans)
        Assertions.assertEquals(emptyList<Int>(), schemaInstance.numbers)
        Assertions.assertEquals(emptyList<MyConcepts.MyEnum>(), schemaInstance.enumerations)
        Assertions.assertEquals(emptyList<ConceptIdentifier>(), schemaInstance.references.ids())
    }

    @Test
    fun `test insert to the same text facet multiple times with addFacetValue does append`() {
        val schemaInstance =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                addReferencedConcepts(schemaContext)

                schemaContext.withRootInstance<MyConcepts> { conceptData ->
                    conceptData.addFacetValue(MyConcepts::texts, "hallo1")
                    conceptData.addFacetValue(MyConcepts::texts, "hallo2")
                    conceptData.addFacetValue(MyConcepts::texts, "hallo3")

                    conceptData.addFacetValue(MyConcepts::booleans, true)
                    conceptData.addFacetValue(MyConcepts::booleans, false)
                    conceptData.addFacetValue(MyConcepts::booleans, true)

                    conceptData.addFacetValue(MyConcepts::numbers, 1)
                    conceptData.addFacetValue(MyConcepts::numbers, 2)
                    conceptData.addFacetValue(MyConcepts::numbers, 3)

                    conceptData.addFacetValue(MyConcepts::enumerations, FOO)
                    conceptData.addFacetValue(MyConcepts::enumerations, BAR)
                    conceptData.addFacetValue(MyConcepts::enumerations, FOO)

                    conceptData.addFacetValue(MyConcepts::references, alpha1Id)
                    conceptData.addFacetValue(MyConcepts::references, beta1Id)
                    conceptData.addFacetValue(MyConcepts::references, alpha2Id)
                }
            }

        Assertions.assertEquals(listOf("hallo1", "hallo2", "hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true, false, true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(1, 2, 3), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO, BAR, FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha1Id, beta1Id, alpha2Id), schemaInstance.references.ids())
    }

    @Test
    fun `test insert a list of strings to text facet with addFacetValues does append`() {
        val schemaInstance =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                addReferencedConcepts(schemaContext)

                schemaContext.withRootInstance<MyConcepts> { conceptData ->
                    conceptData.addFacetValues(MyConcepts::texts, listOf("hallo1", "hallo2"))
                    conceptData.addFacetValues(MyConcepts::texts, listOf("hallo3"))

                    conceptData.addFacetValues(MyConcepts::booleans, listOf(true, false))
                    conceptData.addFacetValues(MyConcepts::booleans, listOf(true))

                    conceptData.addFacetValues(MyConcepts::numbers, listOf(1, 2))
                    conceptData.addFacetValues(MyConcepts::numbers, listOf(3))

                    conceptData.addFacetValues(MyConcepts::enumerations, listOf(FOO, BAR))
                    conceptData.addFacetValues(MyConcepts::enumerations, listOf(FOO))

                    conceptData.addFacetValues(MyConcepts::references, listOf(alpha1Id, beta1Id))
                    conceptData.addFacetValues(MyConcepts::references, listOf(alpha2Id))
                }
            }

        Assertions.assertEquals(listOf("hallo1", "hallo2", "hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true, false, true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(1, 2, 3), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO, BAR, FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha1Id, beta1Id, alpha2Id), schemaInstance.references.ids())
    }

    @Test
    fun `test insert an empty list of strings to text facet addFacetValues does not change the facet values`() {
        val schemaInstance =
            SchemaApi.withSchema<MyConcepts> { schemaContext ->
                addReferencedConcepts(schemaContext)

                schemaContext.withRootInstance<MyConcepts> { conceptData ->
                    conceptData.addFacetValues(MyConcepts::texts, emptyList())
                    conceptData.addFacetValues(MyConcepts::texts, listOf("hallo1"))
                    conceptData.addFacetValues(MyConcepts::texts, emptyList())

                    conceptData.addFacetValues(MyConcepts::booleans, emptyList())
                    conceptData.addFacetValues(MyConcepts::booleans, listOf(true))
                    conceptData.addFacetValues(MyConcepts::booleans, emptyList())

                    conceptData.addFacetValues(MyConcepts::numbers, emptyList())
                    conceptData.addFacetValues(MyConcepts::numbers, listOf(1))
                    conceptData.addFacetValues(MyConcepts::numbers, emptyList())

                    conceptData.addFacetValues(MyConcepts::enumerations, emptyList())
                    conceptData.addFacetValues(MyConcepts::enumerations, listOf(FOO))
                    conceptData.addFacetValues(MyConcepts::enumerations, emptyList())

                    conceptData.addFacetValues(MyConcepts::references, emptyList())
                    conceptData.addFacetValues(MyConcepts::references, listOf(beta1Id))
                    conceptData.addFacetValues(MyConcepts::references, emptyList())
                }
            }

        Assertions.assertEquals(listOf("hallo1"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(1), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(beta1Id), schemaInstance.references.ids())
    }
}
