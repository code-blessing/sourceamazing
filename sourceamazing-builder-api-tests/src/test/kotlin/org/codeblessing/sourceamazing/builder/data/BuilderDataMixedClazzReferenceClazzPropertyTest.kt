package org.codeblessing.sourceamazing.builder.data

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderDataMixedClazzReferenceClazzPropertyTest {

    private interface AlphaAndBeta {
        val id: String
    }

    private interface ClazzAlpha : AlphaAndBeta

    private interface ClazzBeta : AlphaAndBeta

    private interface ClazzGamma

    @AdditionallyKnownClasses([ClazzAlpha::class, ClazzBeta::class])
    private interface MyClazzes {

        val alphaAndBetaAsList: List<AlphaAndBeta>

        val gammaAsList: List<ClazzGamma>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = MyClazzes::class, alias = "root")
    private interface BuilderToAddReferences {

        @BuilderMethod
        @NewClazzModel(clazz = ClazzAlpha::class, alias = "alphaClazz")
        fun createAlphaClazz(
            @SetAsClazzModelId(alias = "alphaClazz") clazzModelId: UniqueId,
            @SetAsValue(alias = "alphaClazz", clazzProperty = "id") id: String,
        )

        @BuilderMethod
        @NewClazzModel(clazz = ClazzBeta::class, alias = "betaClazz")
        fun createBetaClazz(
            @SetAsClazzModelId(alias = "betaClazz") clazzModelId: UniqueId,
            @SetAsValue(alias = "betaClazz", clazzProperty = "id") id: String,
        )

        @BuilderMethod
        @NewClazzModel(clazz = ClazzGamma::class, alias = "gammaClazz")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "gammaAsList", referencedAlias = "gammaClazz")
        fun createGammaClazz(@SetAsClazzModelId(alias = "gammaClazz") clazzModelId: UniqueId)

        @BuilderMethod
        fun addReference(
            @SetClazzModelOfId(
                alias = "root",
                clazzProperty = "alphaAndBetaAsList",
                modification = ClazzPropertyModification.ADD,
            )
            clazzModelId: UniqueId
        )
    }

    @Test
    fun `test mixed clazz of alpha and beta references`() {
        val alpha1ClazzModelId = UniqueId.of("Alpha1-Id")
        val alpha2ClazzModelId = UniqueId.of("Alpha2-Id")
        val beta1ClazzModelId = UniqueId.of("Beta1-Id")
        val beta2ClazzModelId = UniqueId.of("Beta2-Id")
        val gamma1ClazzModelId = UniqueId.of("Gamma1-Id")
        val schemaInstance: MyClazzes =
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder.createAlphaClazz(alpha1ClazzModelId, alpha1ClazzModelId.name)
                    builder.createAlphaClazz(alpha2ClazzModelId, alpha2ClazzModelId.name)
                    builder.createBetaClazz(beta1ClazzModelId, beta1ClazzModelId.name)
                    builder.createBetaClazz(beta2ClazzModelId, beta2ClazzModelId.name)
                    builder.createGammaClazz(gamma1ClazzModelId)

                    builder.addReference(alpha1ClazzModelId)
                    builder.addReference(beta1ClazzModelId)
                    builder.addReference(alpha1ClazzModelId)
                    builder.addReference(alpha2ClazzModelId)
                }
            }
        val expectedClazzModelIds =
            listOf(alpha1ClazzModelId, beta1ClazzModelId, alpha1ClazzModelId, alpha2ClazzModelId).map { it.name }

        assertEquals(expectedClazzModelIds, schemaInstance.alphaAndBetaAsList.map { it.id })
    }

    @Test
    fun `test mixed clazz of alpha and beta and invalid gamma references`() {
        val alpha1ClazzModelId = UniqueId.of("Alpha1-Id")
        val alpha2ClazzModelId = UniqueId.of("Alpha2-Id")
        val beta1ClazzModelId = UniqueId.of("Beta1-Id")
        val beta2ClazzModelId = UniqueId.of("Beta2-Id")
        val gamma1ClazzModelId = UniqueId.of("Gamma1-Id")

        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
        ) {
            SchemaApi.withSchema(MyClazzes::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderToAddReferences::class) { builder ->
                    builder.createAlphaClazz(alpha1ClazzModelId, alpha1ClazzModelId.name)
                    builder.createAlphaClazz(alpha2ClazzModelId, alpha2ClazzModelId.name)
                    builder.createBetaClazz(beta1ClazzModelId, beta1ClazzModelId.name)
                    builder.createBetaClazz(beta2ClazzModelId, beta2ClazzModelId.name)
                    builder.createGammaClazz(gamma1ClazzModelId)

                    builder.addReference(alpha1ClazzModelId)
                    builder.addReference(beta1ClazzModelId)
                    builder.addReference(alpha1ClazzModelId)
                    builder.addReference(alpha2ClazzModelId)
                    builder.addReference(gamma1ClazzModelId) // this reference is invalid
                }
            }
        }
    }
}
