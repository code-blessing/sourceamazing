package org.codeblessing.sourceamazing.builder.proxy

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.validation.BuilderHierarchyValidator
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderInvocationHandlerTest {

    @Schema(concepts = [
        BuilderTestSchema.PersonConcept::class,
        BuilderTestSchema.SkillConcept::class,
    ])
    private interface BuilderTestSchema {
        @Concept(facets = [
            PersonConcept.PersonFirstnameFacet::class,
            PersonConcept.PersonAgeFacet::class,
            PersonConcept.PersonSkillsReference::class,
        ])
        interface PersonConcept {

            @StringFacet
            interface PersonFirstnameFacet
            @IntFacet
            interface PersonAgeFacet
            @ReferenceFacet(
                minimumOccurrences = 0,
                maximumOccurrences = 10,
                referencedConcepts = [SkillConcept::class])
            interface PersonSkillsReference

        }
        @Concept(facets = [
            SkillConcept.SkillDescriptionFacet::class,
            SkillConcept.SkillStillEnjoyingFacet::class,
        ])
        interface SkillConcept {
            @StringFacet
            interface SkillDescriptionFacet
            @BooleanFacet
            interface SkillStillEnjoyingFacet
        }
    }

    private val jamesConceptIdentifier = ConceptIdentifier.of("James")
    private val cookingConceptIdentifier = ConceptIdentifier.of("Cooking")
    private val skateboardConceptIdentifier = ConceptIdentifier.of("Skateboard")
    private val lindaConceptIdentifier = ConceptIdentifier.of("Linda")
    private val judoConceptIdentifier = ConceptIdentifier.of("Judo")

    @Builder
    interface RootBuilder {

        // Builder style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = BuilderTestSchema.PersonConcept::class)
        fun newPerson(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(facetToModify = BuilderTestSchema.PersonConcept.PersonFirstnameFacet::class) firstname: String,
        ): PersonConceptBuilder

        // DSL style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = BuilderTestSchema.PersonConcept::class)
        fun newPerson(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @InjectBuilder builder: PersonConceptBuilder.() -> Unit,
        )
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder
    interface PersonConceptBuilder {

        @Suppress("UNUSED")
        @BuilderMethod
        fun firstname(
            @SetFacetValue(facetToModify = BuilderTestSchema.PersonConcept.PersonFirstnameFacet::class) firstname: String,
        ): PersonConceptBuilder

        @BuilderMethod
        fun age(
            @SetFacetValue(facetToModify = BuilderTestSchema.PersonConcept.PersonAgeFacet::class)  age: Int,
        ): PersonConceptBuilder

        @BuilderMethod
        fun firstnameAndAge(
            @SetFacetValue(facetToModify = BuilderTestSchema.PersonConcept.PersonFirstnameFacet::class) firstname: String,
            @SetFacetValue(facetToModify = BuilderTestSchema.PersonConcept.PersonAgeFacet::class)  age: Int,
        ): PersonConceptBuilder

        // Builder style
        @BuilderMethod
        @WithNewBuilder(builderClass = SkillConceptBuilder::class)
        @NewConcept(BuilderTestSchema.SkillConcept::class, declareConceptAlias = "skill")
        @SetAliasConceptIdentifierReferenceFacetValue(facetToModify = BuilderTestSchema.PersonConcept.PersonSkillsReference::class, referencedConceptAlias = "skill")
        fun skill(
            @SetConceptIdentifierValue(conceptToModifyAlias = "skill") skillConceptIdentifier: ConceptIdentifier,
        ): SkillConceptBuilder

        // DSL style
        @BuilderMethod
        @WithNewBuilder(builderClass = SkillConceptBuilder::class)
        @NewConcept(BuilderTestSchema.SkillConcept::class, declareConceptAlias = "skill")
        @SetAliasConceptIdentifierReferenceFacetValue(facetToModify = BuilderTestSchema.PersonConcept.PersonSkillsReference::class, referencedConceptAlias = "skill")
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
            @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = BuilderTestSchema.SkillConcept.SkillDescriptionFacet::class) description: String,
            @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = BuilderTestSchema.SkillConcept.SkillStillEnjoyingFacet::class) stillEnjoying: Boolean,
        ): SkillConceptBuilder

        @BuilderMethod
        fun description(
            @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = BuilderTestSchema.SkillConcept.SkillDescriptionFacet::class) description: String,
        ): SkillConceptBuilder

        @BuilderMethod
        fun stillEnjoying(
            @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = BuilderTestSchema.SkillConcept.SkillStillEnjoyingFacet::class) stillEnjoying: Boolean,
        ): SkillConceptBuilder

    }

    private fun createBuilderProxy(conceptDataCollector: ConceptDataCollector): RootBuilder {
        val schemaAccess = createSchemaAccess()
        BuilderHierarchyValidator.validateTopLevelBuilderMethods(RootBuilder::class, schemaAccess)
        return ProxyCreator.createProxy(
            RootBuilder::class,
            BuilderInvocationHandler(schemaAccess, RootBuilder::class, conceptDataCollector, emptyMap(), emptyMap())
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

    private fun checkAssertions(conceptDataCollector: ConceptDataCollector) {
        val personFirstnameFacet = FacetName.of(BuilderTestSchema.PersonConcept.PersonFirstnameFacet::class)
        val personAgeFacet = FacetName.of(BuilderTestSchema.PersonConcept.PersonAgeFacet::class)
        val personSkillReferenceFacet = FacetName.of(BuilderTestSchema.PersonConcept.PersonSkillsReference::class)
        val skillDescriptionFacet = FacetName.of(BuilderTestSchema.SkillConcept.SkillDescriptionFacet::class)

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

    private fun createDataCollector(): ConceptDataCollector {
        return ConceptDataCollector(createSchemaAccess())
    }

    private fun createSchemaAccess(): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(BuilderTestSchema::class)
    }

}