package org.codeblessing.sourceamazing.engine.process.datacollection.proxy

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private val concept1Name = "Concept1"
private val concept2Name = "Concept2"
private val facet1AlphaName = "Facet1Alpha"
private val facet1BetaName = "Facet1Beta"
private val facet2GammaName = "Facet2Gamma"

private val concept1 = ConceptName.of(concept1Name)
private val concept2 = ConceptName.of(concept2Name)
private val facet1Alpha = FacetName.of(facet1AlphaName)
private val facet1Beta = FacetName.of(facet1BetaName)
private val facet2Gamma = FacetName.of(facet2GammaName)

private val identifier1 = ConceptIdentifier.of("Id1")
private val identifier2 = ConceptIdentifier.of("Id2")

class DataCollectorInvocationHandlerValidationTest {

    @DataCollector
    interface RootCollectorWithUnannotatedFields {

        // Builder style
        @AddConceptAndFacets(conceptBuilderClazz = BuilderWithUnannotatedFields::class)
        fun newConcept(
            /* missing annotation */ conceptIdentifier: ConceptIdentifier,
            @ConceptNameValue conceptName: ConceptName = concept1,
        ): BuilderWithUnannotatedFields
    }

    interface SimpleBuilder {
        @AddFacets
        fun simpleField(
            @DynamicFacetValue simpleField: String,
            @DynamicFacetNameValue facetName: FacetName = FacetName.of(facet1AlphaName),
        ): BuilderWithUnannotatedFields
    }


    @Test
    fun `test root collector with unannotated param fields`() {
        val dataCollectorProxy = createDataCollectorProxy(RootCollectorWithUnannotatedFields::class.java)
        assertThrows<IllegalStateException> {
            dataCollectorProxy
                .newConcept(identifier1)
                .simpleField("James")
        }
    }

    @DataCollector
    interface RootCollectorWithBuilderWithUnannotatedFields {

        // Builder style
        @AddConceptAndFacets(conceptBuilderClazz = BuilderWithUnannotatedFields::class)
        fun newConcept(
            @ConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @ConceptNameValue conceptName: ConceptName = concept1,
        ): BuilderWithUnannotatedFields
    }

    interface BuilderWithUnannotatedFields {
        @AddFacets
        fun simpleField(
            /* missing */ simpleField: String,
            @DynamicFacetNameValue facetName: FacetName = FacetName.of(facet1AlphaName),
        ): BuilderWithUnannotatedFields
    }


    @Test
    fun `test builder collector with unannotated param fields`() {
        val dataCollectorProxy = createDataCollectorProxy(RootCollectorWithBuilderWithUnannotatedFields::class.java)
        assertThrows<IllegalStateException> {
            dataCollectorProxy
                .newConcept(identifier1)
                .simpleField("James")
        }
    }


    private fun <T: Any> createDataCollectorProxy(clazz: Class<T>): T {
        return ProxyCreator.createProxy(clazz,
            DataCollectorInvocationHandler(ConceptDataCollector())
        )
    }

}