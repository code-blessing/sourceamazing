package org.codeblessing.sourceamazing.builder.proxy

import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.validation.BuilderHierarchyValidator
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollectorImpl
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.toConceptName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderInvocationHandlerTest {

    private interface BuilderTestSchema {
        interface PersonConcept {
            @Suppress("UNUSED")
            @Facet
            val firstname: String

            @Suppress("UNUSED")
            @Facet
            val age: Int

            @Suppress("UNUSED")
            @Facet
            val skills: List<SkillConcept>
        }

        interface SkillConcept {
            @Suppress("UNUSED")
            @Facet
            val description: String

            @Suppress("UNUSED")
            @Facet
            val skillEnjoying: Boolean
        }

        @Suppress("UNUSED")
        @Facet
        val person: PersonConcept

    }

    private val jamesConceptIdentifier = ConceptIdentifier.of("James")
    private val cookingConceptIdentifier = ConceptIdentifier.of("Cooking")
    private val skateboardConceptIdentifier = ConceptIdentifier.of("Skateboard")
    private val lindaConceptIdentifier = ConceptIdentifier.of("Linda")
    private val judoConceptIdentifier = ConceptIdentifier.of("Judo")

    @Builder
    @ExpectedRootAlias("root")
    interface PersonBuilder {

        // Builder style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = BuilderTestSchema.PersonConcept::class, declareConceptAlias = "person")
        fun newPerson(
            @SetConceptIdentifierValue(conceptToModifyAlias = "person") conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "firstname") firstname: String,
        ): PersonConceptBuilder

        // DSL style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = BuilderTestSchema.PersonConcept::class, declareConceptAlias = "person")
        fun newPerson(
            @SetConceptIdentifierValue(conceptToModifyAlias = "person") conceptIdentifier: ConceptIdentifier,
            @InjectBuilder builder: PersonConceptBuilder.() -> Unit,
        )
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(conceptAlias = "person")
    interface PersonConceptBuilder {

        @Suppress("UNUSED")
        @BuilderMethod
        fun firstname(
            @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "firstname") firstname: String,
        ): PersonConceptBuilder

        @BuilderMethod
        fun age(
            @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "age") age: Int,
        ): PersonConceptBuilder

        @BuilderMethod
        fun firstnameAndAge(
            @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "firstname") firstname: String,
            @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "age") age: Int,
        ): PersonConceptBuilder

        // Builder style
        @BuilderMethod
        @WithNewBuilder(builderClass = SkillConceptBuilder::class)
        @NewConcept(BuilderTestSchema.SkillConcept::class, declareConceptAlias = "skill")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "person",
            facetToModify = "skills",
            referencedConceptAlias = "skill",
        )
        fun skill(
            @SetConceptIdentifierValue(conceptToModifyAlias = "skill") skillConceptIdentifier: ConceptIdentifier,
        ): SkillConceptBuilder

        // DSL style
        @BuilderMethod
        @WithNewBuilder(builderClass = SkillConceptBuilder::class)
        @NewConcept(BuilderTestSchema.SkillConcept::class, declareConceptAlias = "skill")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "person",
            facetToModify = "skills",
            referencedConceptAlias = "skill",
        )
        fun skill(
            @SetConceptIdentifierValue(conceptToModifyAlias = "skill") skillConceptIdentifier: ConceptIdentifier,
            @InjectBuilder builder: SkillConceptBuilder.() -> Unit,
        )

    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder("skill")
    interface SkillConceptBuilder {

        @BuilderMethod
        fun descriptionAndStillEnjoying(
            @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "description") description: String,
            @SetFacetValue(
                conceptToModifyAlias = "skill",
                facetToModify = "skillEnjoying",
            ) stillEnjoying: Boolean,
        ): SkillConceptBuilder

        @BuilderMethod
        fun description(
            @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "description") description: String,
        ): SkillConceptBuilder

        @BuilderMethod
        fun stillEnjoying(
            @SetFacetValue(
                conceptToModifyAlias = "skill",
                facetToModify = "skillEnjoying",
            ) stillEnjoying: Boolean,
        ): SkillConceptBuilder

    }

    private fun createBuilderProxy(conceptDataCollector: ConceptDataCollectorImpl): PersonBuilder {
        val schemaAccess = createSchemaAccess()
        BuilderHierarchyValidator.validateTopLevelBuilderMethods(
            PersonBuilder::class,
            schemaAccess,
            BuilderTestSchema::class.toConceptName(),
        )
        return ProxyCreator.createProxy(
            PersonBuilder::class,
            BuilderInvocationHandler(schemaAccess, PersonBuilder::class, true, conceptDataCollector, emptyMap(), emptyMap()),
        )
    }

    @Test
    fun `test data invocation with individual data collector in builder style`() {
        val conceptDataCollector = createDataCollector()
        val builderProxy = createBuilderProxy(conceptDataCollector)

        val james = builderProxy
            .newPerson(jamesConceptIdentifier, firstname = "James")
            .age(18)
        james.skill(cookingConceptIdentifier)
            .descriptionAndStillEnjoying("Cooking for Dinner", true)
        james.skill(skateboardConceptIdentifier)
            .description("Skateboarding")
            .stillEnjoying(false)

        val linda = builderProxy
            .newPerson(lindaConceptIdentifier, firstname = "Linda")
            .age(29)
        linda.skill(judoConceptIdentifier)
            .description("Judo")
            .stillEnjoying(true)

        checkAssertions(conceptDataCollector)
    }

    @Test
    fun `test data invocation with individual data collector in DSL style`() {
        val conceptDataCollector = createDataCollector()
        val builderProxy = createBuilderProxy(conceptDataCollector)

        builderProxy
            .newPerson(jamesConceptIdentifier) {
                firstnameAndAge(firstname = "James", age = 18)
                skill(cookingConceptIdentifier) {
                    description("Cooking for Dinner")
                    stillEnjoying(true)
                }
                skill(skateboardConceptIdentifier) {
                    description("Skateboarding")
                    stillEnjoying(false)
                }
            }

        builderProxy
            .newPerson(lindaConceptIdentifier) {
                firstnameAndAge(firstname = "Linda", age = 29)
                skill(judoConceptIdentifier) {
                    description("Judo")
                    stillEnjoying(true)
                }
            }

        checkAssertions(conceptDataCollector)
    }

    private fun checkAssertions(conceptDataCollector: ConceptDataCollectorImpl) {
        val personFirstnameFacet = FacetName.of("firstname")
        val personAgeFacet = FacetName.of("age")
        val personSkillReferenceFacet = FacetName.of("skills")
        val skillDescriptionFacet = FacetName.of("description")

        val conceptDataList = conceptDataCollector.provideConceptData()
        Assertions.assertEquals(5, conceptDataList.size)

        val james = conceptDataList
            .single { it.conceptIdentifier == jamesConceptIdentifier }
        Assertions.assertEquals("James", james.getFacet(personFirstnameFacet).single())
        Assertions.assertEquals(18, james.getFacet(personAgeFacet).single())
        Assertions.assertEquals(2, james.getFacet(personSkillReferenceFacet).size)

        val linda = conceptDataList
            .single { it.conceptIdentifier == lindaConceptIdentifier }
        Assertions.assertEquals("Linda", linda.getFacet(personFirstnameFacet).single())
        Assertions.assertEquals(judoConceptIdentifier, linda.getFacet(personSkillReferenceFacet).single())

        val judo = conceptDataList
            .single { it.conceptIdentifier == judoConceptIdentifier }

        Assertions.assertEquals("Judo", judo.getFacet(skillDescriptionFacet).single())
    }

    private fun createDataCollector(): ConceptDataCollectorImpl {
        return ConceptDataCollectorImpl(createSchemaAccess())
    }

    private fun createSchemaAccess(): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(BuilderTestSchema::class)
    }

}
