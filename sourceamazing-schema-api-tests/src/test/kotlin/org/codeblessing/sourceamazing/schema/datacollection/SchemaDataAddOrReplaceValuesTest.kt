package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.datacollection.newClazzModel
import org.codeblessing.sourceamazing.schema.datacollection.SchemaDataAddOrReplaceValuesTest.MyEnum.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaDataAddOrReplaceValuesTest {
    private val alpha1Id = UniqueId.of("Alpha1-Id")
    private val alpha2Id = UniqueId.of("Alpha2-Id")
    private val beta1Id = UniqueId.of("Beta1-Id")

    private val sealedAlpha1Id = UniqueId.of("Sealed-Alpha1-Id")
    private val sealedAlpha2Id = UniqueId.of("Sealed-Alpha2-Id")
    private val sealedBeta1Id = UniqueId.of("Sealed-Beta1-Id")

    enum class MyEnum {
        FOO,
        BAR,
    }

    private interface AlphaAndBeta {
        val id: String
    }

    private interface ClazzAlpha : AlphaAndBeta

    private interface ClazzBeta : AlphaAndBeta

    private sealed interface SealedAlphaAndBeta {
        val id: String
    }

    private interface SealedClazzAlpha : SealedAlphaAndBeta

    private interface SealedClazzBeta : SealedAlphaAndBeta

    @AdditionallyKnownClasses([ClazzAlpha::class, ClazzBeta::class])
    private interface MyClazzes {

        val texts: List<String>

        val booleans: List<Boolean>

        val numbers: List<Int>

        val enumerations: List<MyEnum>

        val references: List<AlphaAndBeta>

        val sealedReferences: List<SealedAlphaAndBeta>
    }

    private fun addReferencedClazzes(schemaContext: SchemaContext) {
        schemaContext.dataCollector.newClazzModel<ClazzAlpha>(alpha1Id).addClazzPropertyValue("id", alpha1Id.name)
        schemaContext.dataCollector.newClazzModel<ClazzAlpha>(alpha2Id).addClazzPropertyValue("id", alpha2Id.name)
        schemaContext.dataCollector.newClazzModel<ClazzBeta>(beta1Id).addClazzPropertyValue("id", beta1Id.name)
    }

    private fun List<AlphaAndBeta>.ids(): List<UniqueId> = this.map { UniqueId.of(it.id) }

    private fun addSealedReferencedClazzes(schemaContext: SchemaContext) {
        schemaContext.dataCollector
            .newClazzModel<SealedClazzAlpha>(sealedAlpha1Id)
            .addClazzPropertyValue("id", sealedAlpha1Id.name)
        schemaContext.dataCollector
            .newClazzModel<SealedClazzAlpha>(sealedAlpha2Id)
            .addClazzPropertyValue("id", sealedAlpha2Id.name)
        schemaContext.dataCollector
            .newClazzModel<SealedClazzBeta>(sealedBeta1Id)
            .addClazzPropertyValue("id", sealedBeta1Id.name)
    }

    private fun List<SealedAlphaAndBeta>.sealedIds(): List<UniqueId> = this.map { UniqueId.of(it.id) }

    @Test
    fun `test insert to the same text clazzProperty multiple times with replaceClazzPropertyValues does always clear and override the result`() {
        val schemaInstance =
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                addReferencedClazzes(schemaContext)
                addSealedReferencedClazzes(schemaContext)

                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.replaceWithClazzPropertyValues("texts", listOf("hallo1"))
                    clazzData.replaceWithClazzPropertyValues("texts", listOf("hallo2"))
                    clazzData.replaceWithClazzPropertyValues("texts", listOf("hallo3"))

                    clazzData.replaceWithClazzPropertyValues("booleans", listOf(true))
                    clazzData.replaceWithClazzPropertyValues("booleans", listOf(false))
                    clazzData.replaceWithClazzPropertyValues("booleans", listOf(true))

                    clazzData.replaceWithClazzPropertyValues("numbers", listOf(1))
                    clazzData.replaceWithClazzPropertyValues("numbers", listOf(2))
                    clazzData.replaceWithClazzPropertyValues("numbers", listOf(3))

                    clazzData.replaceWithClazzPropertyValues("enumerations", listOf(FOO))
                    clazzData.replaceWithClazzPropertyValues("enumerations", listOf(BAR))
                    clazzData.replaceWithClazzPropertyValues("enumerations", listOf(FOO))

                    clazzData.replaceWithClazzPropertyReferences("references", listOf(alpha1Id))
                    clazzData.replaceWithClazzPropertyReferences("references", listOf(beta1Id))
                    clazzData.replaceWithClazzPropertyReferences("references", listOf(alpha2Id))

                    clazzData.replaceWithClazzPropertyReferences("sealedReferences", listOf(sealedAlpha1Id))
                    clazzData.replaceWithClazzPropertyReferences("sealedReferences", listOf(sealedBeta1Id))
                    clazzData.replaceWithClazzPropertyReferences("sealedReferences", listOf(sealedAlpha2Id))
                }
            }

        Assertions.assertEquals(listOf("hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(3), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha2Id), schemaInstance.references.ids())
        Assertions.assertEquals(listOf(sealedAlpha2Id), schemaInstance.sealedReferences.sealedIds())
    }

    @Test
    fun `test insert a list of strings to text clazzProperty with replaceClazzPropertyValues does replace with all list entries`() {
        val schemaInstance =
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                addReferencedClazzes(schemaContext)
                addSealedReferencedClazzes(schemaContext)

                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.replaceWithClazzPropertyValues("texts", listOf("hallo1", "hallo2"))
                    clazzData.replaceWithClazzPropertyValues("texts", listOf("hallo2", "hallo3"))

                    clazzData.replaceWithClazzPropertyValues("booleans", listOf(true, false))
                    clazzData.replaceWithClazzPropertyValues("booleans", listOf(false, true))
                    clazzData.replaceWithClazzPropertyValues("booleans", listOf(true, false))

                    clazzData.replaceWithClazzPropertyValues("numbers", listOf(1, 2))
                    clazzData.replaceWithClazzPropertyValues("numbers", listOf(2, 3))
                    clazzData.replaceWithClazzPropertyValues("numbers", listOf(3, 4))

                    clazzData.replaceWithClazzPropertyValues("enumerations", listOf(FOO, BAR))
                    clazzData.replaceWithClazzPropertyValues("enumerations", listOf(BAR, FOO))
                    clazzData.replaceWithClazzPropertyValues("enumerations", listOf(FOO, BAR))

                    clazzData.replaceWithClazzPropertyReferences("references", listOf(alpha1Id, beta1Id))
                    clazzData.replaceWithClazzPropertyReferences("references", listOf(beta1Id, alpha2Id))
                    clazzData.replaceWithClazzPropertyReferences("references", listOf(alpha2Id, beta1Id))

                    clazzData.replaceWithClazzPropertyReferences(
                        "sealedReferences",
                        listOf(sealedAlpha1Id, sealedBeta1Id),
                    )
                    clazzData.replaceWithClazzPropertyReferences(
                        "sealedReferences",
                        listOf(sealedBeta1Id, sealedAlpha2Id),
                    )
                    clazzData.replaceWithClazzPropertyReferences(
                        "sealedReferences",
                        listOf(sealedAlpha2Id, sealedBeta1Id),
                    )
                }
            }

        Assertions.assertEquals(listOf("hallo2", "hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true, false), schemaInstance.booleans)
        Assertions.assertEquals(listOf(3, 4), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO, BAR), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha2Id, beta1Id), schemaInstance.references.ids())
        Assertions.assertEquals(listOf(sealedAlpha2Id, sealedBeta1Id), schemaInstance.sealedReferences.sealedIds())
    }

    @Test
    fun `test insert an empty list of strings to text clazzProperty with replaceClazzPropertyValues does replace with an empty list`() {
        val schemaInstance =
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                addReferencedClazzes(schemaContext)
                addSealedReferencedClazzes(schemaContext)

                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.replaceWithClazzPropertyValues("texts", listOf("hallo1", "hallo2"))
                    clazzData.replaceWithClazzPropertyValues("texts", emptyList())

                    clazzData.replaceWithClazzPropertyValues("booleans", listOf(true, false))
                    clazzData.replaceWithClazzPropertyValues("booleans", emptyList())

                    clazzData.replaceWithClazzPropertyValues("numbers", listOf(1, 2))
                    clazzData.replaceWithClazzPropertyValues("numbers", emptyList())

                    clazzData.replaceWithClazzPropertyValues("enumerations", listOf(FOO, BAR))
                    clazzData.replaceWithClazzPropertyValues("enumerations", emptyList())

                    clazzData.replaceWithClazzPropertyReferences("references", listOf(alpha1Id, beta1Id))
                    clazzData.replaceWithClazzPropertyReferences("references", emptyList())

                    clazzData.replaceWithClazzPropertyReferences(
                        "sealedReferences",
                        listOf(sealedAlpha1Id, sealedBeta1Id),
                    )
                    clazzData.replaceWithClazzPropertyReferences("sealedReferences", emptyList())
                }
            }

        Assertions.assertEquals(emptyList<String>(), schemaInstance.texts)
        Assertions.assertEquals(emptyList<Boolean>(), schemaInstance.booleans)
        Assertions.assertEquals(emptyList<Int>(), schemaInstance.numbers)
        Assertions.assertEquals(emptyList<MyEnum>(), schemaInstance.enumerations)
        Assertions.assertEquals(emptyList<UniqueId>(), schemaInstance.references.ids())
        Assertions.assertEquals(emptyList<UniqueId>(), schemaInstance.sealedReferences.sealedIds())
    }

    @Test
    fun `test insert to the same text clazzProperty multiple times with addClazzPropertyValue does append`() {
        val schemaInstance =
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                addReferencedClazzes(schemaContext)
                addSealedReferencedClazzes(schemaContext)

                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.addClazzPropertyValue("texts", "hallo1")
                    clazzData.addClazzPropertyValue("texts", "hallo2")
                    clazzData.addClazzPropertyValue("texts", "hallo3")

                    clazzData.addClazzPropertyValue("booleans", true)
                    clazzData.addClazzPropertyValue("booleans", false)
                    clazzData.addClazzPropertyValue("booleans", true)

                    clazzData.addClazzPropertyValue("numbers", 1)
                    clazzData.addClazzPropertyValue("numbers", 2)
                    clazzData.addClazzPropertyValue("numbers", 3)

                    clazzData.addClazzPropertyValue("enumerations", FOO)
                    clazzData.addClazzPropertyValue("enumerations", BAR)
                    clazzData.addClazzPropertyValue("enumerations", FOO)

                    clazzData.addClazzPropertyReference("references", alpha1Id)
                    clazzData.addClazzPropertyReference("references", beta1Id)
                    clazzData.addClazzPropertyReference("references", alpha2Id)

                    clazzData.addClazzPropertyReference("sealedReferences", sealedAlpha1Id)
                    clazzData.addClazzPropertyReference("sealedReferences", sealedBeta1Id)
                    clazzData.addClazzPropertyReference("sealedReferences", sealedAlpha2Id)
                }
            }

        Assertions.assertEquals(listOf("hallo1", "hallo2", "hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true, false, true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(1, 2, 3), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO, BAR, FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha1Id, beta1Id, alpha2Id), schemaInstance.references.ids())
        Assertions.assertEquals(
            listOf(sealedAlpha1Id, sealedBeta1Id, sealedAlpha2Id),
            schemaInstance.sealedReferences.sealedIds(),
        )
    }

    @Test
    fun `test insert a list of strings to text clazzProperty with addClazzPropertyValues does append`() {
        val schemaInstance =
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                addReferencedClazzes(schemaContext)
                addSealedReferencedClazzes(schemaContext)

                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.addClazzPropertyValues("texts", listOf("hallo1", "hallo2"))
                    clazzData.addClazzPropertyValues("texts", listOf("hallo3"))

                    clazzData.addClazzPropertyValues("booleans", listOf(true, false))
                    clazzData.addClazzPropertyValues("booleans", listOf(true))

                    clazzData.addClazzPropertyValues("numbers", listOf(1, 2))
                    clazzData.addClazzPropertyValues("numbers", listOf(3))

                    clazzData.addClazzPropertyValues("enumerations", listOf(FOO, BAR))
                    clazzData.addClazzPropertyValues("enumerations", listOf(FOO))

                    clazzData.addClazzPropertyReferences("references", listOf(alpha1Id, beta1Id))
                    clazzData.addClazzPropertyReferences("references", listOf(alpha2Id))

                    clazzData.addClazzPropertyReferences("sealedReferences", listOf(sealedAlpha1Id, sealedBeta1Id))
                    clazzData.addClazzPropertyReferences("sealedReferences", listOf(sealedAlpha2Id))
                }
            }

        Assertions.assertEquals(listOf("hallo1", "hallo2", "hallo3"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true, false, true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(1, 2, 3), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO, BAR, FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(alpha1Id, beta1Id, alpha2Id), schemaInstance.references.ids())
        Assertions.assertEquals(
            listOf(sealedAlpha1Id, sealedBeta1Id, sealedAlpha2Id),
            schemaInstance.sealedReferences.sealedIds(),
        )
    }

    @Test
    fun `test insert an empty list of strings to text clazzProperty addClazzPropertyValues does not change the clazzProperty values`() {
        val schemaInstance =
            SchemaApi.withSchema<MyClazzes> { schemaContext ->
                addReferencedClazzes(schemaContext)
                addSealedReferencedClazzes(schemaContext)

                schemaContext.dataCollector.rootClazzModel().let { clazzData ->
                    clazzData.addClazzPropertyValues("texts", emptyList())
                    clazzData.addClazzPropertyValues("texts", listOf("hallo1"))
                    clazzData.addClazzPropertyValues("texts", emptyList())

                    clazzData.addClazzPropertyValues("booleans", emptyList())
                    clazzData.addClazzPropertyValues("booleans", listOf(true))
                    clazzData.addClazzPropertyValues("booleans", emptyList())

                    clazzData.addClazzPropertyValues("numbers", emptyList())
                    clazzData.addClazzPropertyValues("numbers", listOf(1))
                    clazzData.addClazzPropertyValues("numbers", emptyList())

                    clazzData.addClazzPropertyValues("enumerations", emptyList())
                    clazzData.addClazzPropertyValues("enumerations", listOf(FOO))
                    clazzData.addClazzPropertyValues("enumerations", emptyList())

                    clazzData.addClazzPropertyReferences("references", emptyList())
                    clazzData.addClazzPropertyReferences("references", listOf(beta1Id))
                    clazzData.addClazzPropertyReferences("references", emptyList())

                    clazzData.addClazzPropertyReferences("sealedReferences", emptyList())
                    clazzData.addClazzPropertyReferences("sealedReferences", listOf(sealedBeta1Id))
                    clazzData.addClazzPropertyReferences("sealedReferences", emptyList())
                }
            }

        Assertions.assertEquals(listOf("hallo1"), schemaInstance.texts)
        Assertions.assertEquals(listOf(true), schemaInstance.booleans)
        Assertions.assertEquals(listOf(1), schemaInstance.numbers)
        Assertions.assertEquals(listOf(FOO), schemaInstance.enumerations)
        Assertions.assertEquals(listOf(beta1Id), schemaInstance.references.ids())
        Assertions.assertEquals(listOf(sealedBeta1Id), schemaInstance.sealedReferences.sealedIds())
    }
}
