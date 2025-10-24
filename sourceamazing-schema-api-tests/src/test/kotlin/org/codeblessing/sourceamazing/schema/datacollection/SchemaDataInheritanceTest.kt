package org.codeblessing.sourceamazing.schema.datacollection

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.api.datacollection.newClazzModel
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaDataInheritanceTest {
    private interface AlphaAndBetaAndGamma {
        val id: String
    }

    private val alphaAndBetaAndGammaInstance: AlphaAndBetaAndGamma =
        object : AlphaAndBetaAndGamma {
            override val id: String = "AlphaAndBetaAndGammaInstance"
        }

    private interface AlphaAndBeta : AlphaAndBetaAndGamma {
        override val id: String
    }

    private interface BetaAndGamma : AlphaAndBetaAndGamma {
        override val id: String
    }

    private val alphaAndBetaInstance: AlphaAndBeta =
        object : AlphaAndBeta {
            override val id: String = "AlphaAndBetaInstance"
        }

    private interface ClazzAlpha : AlphaAndBeta

    private val alphaInstance: ClazzAlpha =
        object : ClazzAlpha {
            override val id: String = "AlphaInstance"
        }

    private interface ClazzBeta : AlphaAndBeta, BetaAndGamma

    private val betaInstance: ClazzBeta =
        object : ClazzBeta {
            override val id: String = "BetaInstance"
        }

    private val betaAndGammaInstance: BetaAndGamma =
        object : BetaAndGamma {
            override val id: String = "BetaAndGammaInstance"
        }

    private interface ClazzGamma : BetaAndGamma

    private val gammaInstance: ClazzGamma =
        object : ClazzGamma {
            override val id: String = "GammaInstance"
        }

    private sealed interface SealedAlphaAndBeta {
        val id: String
    }

    private interface SealedClazzAlpha : SealedAlphaAndBeta

    private val sealedAlphaInstance: SealedClazzAlpha =
        object : SealedClazzAlpha {
            override val id: String = "SealedAlphaInstance"
        }

    private interface SealedClazzBeta : SealedAlphaAndBeta

    private val sealedBetaInstance: SealedClazzBeta =
        object : SealedClazzBeta {
            override val id: String = "SealedBetaInstance"
        }

    @AdditionallyKnownClasses([ClazzAlpha::class, ClazzBeta::class])
    private interface MyInheritableListsClazzes {

        val inheritanceClazzes: List<AlphaAndBeta>
        val sealedInheritanceClazzes: List<SealedAlphaAndBeta>
    }

    @Test
    fun `test mixing inheritable clazz models and inheritable resolved instances`() {
        val schemaInstance =
            SchemaApi.withSchema<MyInheritableListsClazzes> { schemaContext ->
                val alphaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<ClazzAlpha>()
                        .addClazzPropertyValue("id", "AlphaClazzModel")

                val betaClazzModel =
                    schemaContext.dataCollector.newClazzModel<ClazzBeta>().addClazzPropertyValue("id", "BetaClazzModel")

                val alphaAndBetaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<AlphaAndBeta>()
                        .addClazzPropertyValue("id", "AlphaAndBetaClazzModel")

                val sealedAlphaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<SealedClazzAlpha>()
                        .addClazzPropertyValue("id", "SealedAlphaClazzModel")

                val sealedBetaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<SealedClazzBeta>()
                        .addClazzPropertyValue("id", "SealedBetaClazzModel")

                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyReference("inheritanceClazzes", alphaClazzModel)
                    .addClazzPropertyReference("inheritanceClazzes", betaClazzModel)
                    .addClazzPropertyReference("inheritanceClazzes", alphaAndBetaClazzModel)
                    .addClazzPropertyValue("inheritanceClazzes", alphaAndBetaInstance)
                    .addClazzPropertyValue("inheritanceClazzes", alphaInstance)
                    .addClazzPropertyValue("inheritanceClazzes", betaInstance)
                    .addClazzPropertyReference("sealedInheritanceClazzes", sealedAlphaClazzModel)
                    .addClazzPropertyReference("sealedInheritanceClazzes", sealedBetaClazzModel)
                    .addClazzPropertyValue("sealedInheritanceClazzes", sealedAlphaInstance)
                    .addClazzPropertyValue("sealedInheritanceClazzes", sealedBetaInstance)
            }

        Assertions.assertEquals(
            listOf(
                "AlphaClazzModel",
                "BetaClazzModel",
                "AlphaAndBetaClazzModel",
                "AlphaAndBetaInstance",
                "AlphaInstance",
                "BetaInstance",
            ),
            schemaInstance.inheritanceClazzes.map { it.id },
        )

        Assertions.assertEquals(
            listOf("SealedAlphaClazzModel", "SealedBetaClazzModel", "SealedAlphaInstance", "SealedBetaInstance"),
            schemaInstance.sealedInheritanceClazzes.map { it.id },
        )
    }

    private interface MyMultipleInheritableListsClazzes {

        val alphaClazzes: List<ClazzAlpha>
        val betaClazzes: List<ClazzBeta>
        val gammaClazzes: List<ClazzGamma>
        val alphaAndBetaClazzes: List<AlphaAndBeta>
        val betaAndGammaClazzes: List<BetaAndGamma>
        val alphaAndBetaAndGammaClazzes: List<AlphaAndBetaAndGamma>
    }

    @Test
    fun `test mixing inheritable clazz models and inheritable resolved instances over three inheritance levels`() {
        val schemaInstance =
            SchemaApi.withSchema<MyMultipleInheritableListsClazzes> { schemaContext ->
                val alphaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<ClazzAlpha>()
                        .addClazzPropertyValue("id", "AlphaClazzModel")

                val betaClazzModel =
                    schemaContext.dataCollector.newClazzModel<ClazzBeta>().addClazzPropertyValue("id", "BetaClazzModel")

                val gammaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<ClazzGamma>()
                        .addClazzPropertyValue("id", "GammaClazzModel")

                val alphaAndBetaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<AlphaAndBeta>()
                        .addClazzPropertyValue("id", "AlphaAndBetaClazzModel")

                val betaAndGammaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<BetaAndGamma>()
                        .addClazzPropertyValue("id", "BetaAndGammaClazzModel")

                val alphaAndBetaAndGammaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<AlphaAndBetaAndGamma>()
                        .addClazzPropertyValue("id", "AlphaAndBetaAndGammaClazzModel")

                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyReference("alphaClazzes", alphaClazzModel)
                    .addClazzPropertyValue("alphaClazzes", alphaInstance)
                    .addClazzPropertyReference("betaClazzes", betaClazzModel)
                    .addClazzPropertyValue("betaClazzes", betaInstance)
                    .addClazzPropertyReference("gammaClazzes", gammaClazzModel)
                    .addClazzPropertyValue("gammaClazzes", gammaInstance)
                    .addClazzPropertyReference("alphaAndBetaClazzes", alphaAndBetaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaClazzes", alphaAndBetaInstance)
                    .addClazzPropertyReference("alphaAndBetaClazzes", alphaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaClazzes", alphaInstance)
                    .addClazzPropertyReference("alphaAndBetaClazzes", betaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaClazzes", betaInstance)
                    .addClazzPropertyReference("betaAndGammaClazzes", betaAndGammaClazzModel)
                    .addClazzPropertyValue("betaAndGammaClazzes", betaAndGammaInstance)
                    .addClazzPropertyReference("betaAndGammaClazzes", betaClazzModel)
                    .addClazzPropertyValue("betaAndGammaClazzes", betaInstance)
                    .addClazzPropertyReference("betaAndGammaClazzes", gammaClazzModel)
                    .addClazzPropertyValue("betaAndGammaClazzes", gammaInstance)
                    .addClazzPropertyReference("alphaAndBetaAndGammaClazzes", alphaAndBetaAndGammaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaAndGammaClazzes", alphaAndBetaAndGammaInstance)
                    .addClazzPropertyReference("alphaAndBetaAndGammaClazzes", alphaAndBetaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaAndGammaClazzes", alphaAndBetaInstance)
                    .addClazzPropertyReference("alphaAndBetaAndGammaClazzes", alphaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaAndGammaClazzes", alphaInstance)
                    .addClazzPropertyReference("alphaAndBetaAndGammaClazzes", betaAndGammaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaAndGammaClazzes", betaAndGammaInstance)
                    .addClazzPropertyReference("alphaAndBetaAndGammaClazzes", betaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaAndGammaClazzes", betaInstance)
                    .addClazzPropertyReference("alphaAndBetaAndGammaClazzes", gammaClazzModel)
                    .addClazzPropertyValue("alphaAndBetaAndGammaClazzes", gammaInstance)
            }

        Assertions.assertEquals(listOf("AlphaClazzModel", "AlphaInstance"), schemaInstance.alphaClazzes.map { it.id })
        Assertions.assertEquals(listOf("BetaClazzModel", "BetaInstance"), schemaInstance.betaClazzes.map { it.id })
        Assertions.assertEquals(listOf("GammaClazzModel", "GammaInstance"), schemaInstance.gammaClazzes.map { it.id })
        Assertions.assertEquals(
            listOf(
                "AlphaAndBetaClazzModel",
                "AlphaAndBetaInstance",
                "AlphaClazzModel",
                "AlphaInstance",
                "BetaClazzModel",
                "BetaInstance",
            ),
            schemaInstance.alphaAndBetaClazzes.map { it.id },
        )
        Assertions.assertEquals(
            listOf(
                "BetaAndGammaClazzModel",
                "BetaAndGammaInstance",
                "BetaClazzModel",
                "BetaInstance",
                "GammaClazzModel",
                "GammaInstance",
            ),
            schemaInstance.betaAndGammaClazzes.map { it.id },
        )
        Assertions.assertEquals(
            listOf(
                "AlphaAndBetaAndGammaClazzModel",
                "AlphaAndBetaAndGammaInstance",
                "AlphaAndBetaClazzModel",
                "AlphaAndBetaInstance",
                "AlphaClazzModel",
                "AlphaInstance",
                "BetaAndGammaClazzModel",
                "BetaAndGammaInstance",
                "BetaClazzModel",
                "BetaInstance",
                "GammaClazzModel",
                "GammaInstance",
            ),
            schemaInstance.alphaAndBetaAndGammaClazzes.map { it.id },
        )
    }

    @Test
    fun `test adding not compatible clazz model should throw a validation exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
        ) {
            SchemaApi.withSchema<MyMultipleInheritableListsClazzes> { schemaContext ->
                val alphaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<ClazzAlpha>()
                        .addClazzPropertyValue("id", "AlphaClazzModel")

                schemaContext.dataCollector.rootClazzModel().addClazzPropertyReference("betaClazzes", alphaClazzModel)
            }
        }
    }

    @Test
    fun `test adding not compatible clazz instance should throw a validation exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema<MyMultipleInheritableListsClazzes> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("betaClazzes", alphaInstance)
            }
        }
    }

    @Test
    fun `test adding not compatible subtype clazz model should throw a validation exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
        ) {
            SchemaApi.withSchema<MyMultipleInheritableListsClazzes> { schemaContext ->
                val alphaClazzModel =
                    schemaContext.dataCollector
                        .newClazzModel<ClazzAlpha>()
                        .addClazzPropertyValue("id", "AlphaClazzModel")

                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyReference("betaAndGammaClazzes", alphaClazzModel)
            }
        }
    }

    @Test
    fun `test adding not compatible subtype clazz instance should throw a validation exception`() {
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema<MyMultipleInheritableListsClazzes> { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("betaAndGammaClazzes", alphaInstance)
            }
        }
    }
}
