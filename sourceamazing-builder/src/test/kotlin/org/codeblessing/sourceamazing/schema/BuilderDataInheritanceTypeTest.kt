package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.toConceptNameAndIdentifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuilderDataInheritanceTypeTest {

    private interface MyConcepts {

        interface MyConcept {
            val texts: List<String>
        }

        val concepts: List<MyConcept>
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts.MyConcept::class, conceptAlias = "myConcept")
    interface NestedBuilder {
        @BuilderMethod
        fun setText(
            @SetFacetValue(
                conceptToModifyAlias = "myConcept",
                facetToModify = "texts",
                facetModificationRule = FacetModificationRule.ADD,
            )
            textValue: String
        )
    }

    private interface BuilderWithTypeParameter<P, R> {

        @BuilderMethod
        @NewConcept(concept = MyConcepts.MyConcept::class, declareConceptAlias = "myConcept")
        @SetRandomConceptIdentifierValue("myConcept")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "concepts",
            referencedConceptAlias = "myConcept",
        )
        fun createConcept(
            @SetFacetValue(
                conceptToModifyAlias = "myConcept",
                facetToModify = "texts",
                facetModificationRule = FacetModificationRule.ADD,
            )
            value: P
        ): R
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = MyConcepts::class, conceptAlias = "root")
    private interface BuilderExtendingInterfaceWithTypeParameter : BuilderWithTypeParameter<String, NestedBuilder>

    @Test
    fun `test using a sub-builder declared as type parameter`() {
        val schemaInstance: MyConcepts =
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<MyConcepts> { conceptData ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderExtendingInterfaceWithTypeParameter::class,
                        mapOf("root" to conceptData.toConceptNameAndIdentifier()),
                    ) { builder ->
                        builder.createConcept("myFirstText").setText("mySecondText")
                    }
                }
            }
        assertEquals(1, schemaInstance.concepts.size)

        val myConcept = schemaInstance.concepts.first()
        assertEquals("myFirstText", myConcept.texts.first())
        assertEquals("mySecondText", myConcept.texts.last())
    }
}
