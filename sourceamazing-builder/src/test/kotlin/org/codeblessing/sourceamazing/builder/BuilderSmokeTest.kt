package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderSmokeTest.SmokeTestSchema.PersonConcept.PersonSex
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderDataProvider
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.ProvideBuilderData
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderSmokeTest {

    @Schema(concepts = [
        SmokeTestSchema.PersonConcept::class,
        SmokeTestSchema.SkillConcept::class,
    ])
    interface SmokeTestSchema {
        @Concept(facets = [
            PersonConcept.PersonFirstnameFacet::class,
            PersonConcept.PersonAgeFacet::class,
            PersonConcept.PersonSexFacet::class
        ])
        interface PersonConcept {

            enum class PersonSex { MALE, FEMALE }

            @StringFacet() interface PersonFirstnameFacet
            @IntFacet
            interface PersonAgeFacet
            @EnumFacet(enumerationClass = PersonSex::class) interface PersonSexFacet

            @QueryConceptIdentifierValue
            fun getConceptId(): ConceptIdentifier

            @QueryFacetValue(PersonFirstnameFacet::class)
            fun getFirstname(): String

            @QueryFacetValue(PersonAgeFacet::class)
            fun getAge(): Int

            @QueryFacetValue(PersonSexFacet::class)
            fun getSex(): PersonSex

        }
        @Concept(facets = [
            SkillConcept.SkillDescriptionFacet::class,
            SkillConcept.SkillStillEnjoyingFacet::class,
        ])
        interface SkillConcept {
            @StringFacet() interface SkillDescriptionFacet
            @BooleanFacet() interface SkillStillEnjoyingFacet

            @QueryConceptIdentifierValue
            fun getSkillConceptIdentifier(): String

            @QueryFacetValue(SkillDescriptionFacet::class)
            fun getSkillDescription(): String

            @QueryFacetValue(SkillStillEnjoyingFacet::class)
            fun isStillFullyEnjoyingAboutThatSkill(): Boolean

        }

        @QueryConcepts(conceptClasses = [PersonConcept::class])
        fun getPersonList(): List<PersonConcept>

        @QueryConcepts(conceptClasses = [SkillConcept::class])
        fun getSkills(): List<SkillConcept>

    }

    @Builder
    interface SmokeTestRootBuilder {

        // Builder style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = SmokeTestSchema.PersonConcept::class)
        fun newPerson(
            @ProvideBuilderData conceptIdentifier: PersonConceptIdentifier,
            @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonFirstnameFacet::class) firstname: String,
            @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonSexFacet::class) sex: PersonSex,
        ): PersonConceptBuilder

        // DSL style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = SmokeTestSchema.PersonConcept::class)
        fun newPerson(
            @ProvideBuilderData conceptIdentifier: PersonConceptIdentifier,
            @InjectBuilder builder: PersonConceptBuilder.() -> Unit,
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder
        interface PersonConceptBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun firstname(
                @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonFirstnameFacet::class) firstname: String,
            ): PersonConceptBuilder

            @BuilderMethod
            fun age(
                @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonAgeFacet::class)  age: Int,
            ): PersonConceptBuilder

            @BuilderMethod
            fun sex(
                @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonSexFacet::class)  sex: PersonSex,
            ): PersonConceptBuilder

            @BuilderMethod
            fun firstnameAndAge(
                @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonFirstnameFacet::class) firstname: String,
                @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonAgeFacet::class)  age: Int,
            ): PersonConceptBuilder

            // Builder style
            @BuilderMethod
            @WithNewBuilder(builderClass = SkillConceptBuilder::class)
            @NewConcept(SmokeTestSchema.SkillConcept::class, declareConceptAlias = "skill")
            fun skill(
                @ProvideBuilderData skillConceptIdentifier: SkillConceptIdentifier,
            ): SkillConceptBuilder

            // DSL style
            @BuilderMethod
            @WithNewBuilder(builderClass = SkillConceptBuilder::class)
            @NewConcept(SmokeTestSchema.SkillConcept::class, declareConceptAlias = "skill")
            fun skill(
                @ProvideBuilderData skillConceptIdentifier: SkillConceptIdentifier,
                @InjectBuilder builder: SkillConceptBuilder.() -> Unit,
            )

        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "skill")
        interface SkillConceptBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun descriptionAndStillEnjoying(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = SmokeTestSchema.SkillConcept.SkillDescriptionFacet::class) description: String,
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = SmokeTestSchema.SkillConcept.SkillStillEnjoyingFacet::class) stillEnjoying: Boolean,
            ): SkillConceptBuilder

            @BuilderMethod
            fun description(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = SmokeTestSchema.SkillConcept.SkillDescriptionFacet::class) description: String,
            ): SkillConceptBuilder

            @BuilderMethod
            fun stillEnjoying(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = SmokeTestSchema.SkillConcept.SkillStillEnjoyingFacet::class) stillEnjoying: Boolean,
            ): SkillConceptBuilder

        }
    }

    @BuilderDataProvider
    class PersonConceptIdentifier(private val conceptIdentifier: String) {

        @BuilderData
        @SetProvidedConceptIdentifierValue()
        fun getConceptId() = ConceptIdentifier.of(conceptIdentifier)
    }

    @BuilderDataProvider
    class SkillConceptIdentifier(private val conceptIdentifier: String) {

        @BuilderData
        @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "skill")
        fun getConceptId() = ConceptIdentifier.of(conceptIdentifier)
    }

    @Test
    fun `test sourceamazing builder as smoke test`() {
        val jamesConceptIdentifier = PersonConceptIdentifier("James")
        val cookingConceptIdentifier = SkillConceptIdentifier("Cooking")
        val skateboardConceptIdentifier = SkillConceptIdentifier("Skateboard")
        val lindaConceptIdentifier = PersonConceptIdentifier("Linda")
        val judoConceptIdentifier = SkillConceptIdentifier("Judo")


        val schemaInstance: SmokeTestSchema = SchemaApi.withSchema(schemaDefinitionClass = SmokeTestSchema::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, SmokeTestRootBuilder::class) { builder ->
                // add some data in DSL style
                builder
                    .newPerson(jamesConceptIdentifier) {
                        firstnameAndAge(firstname = "James", age = 18)
                        sex(PersonSex.MALE)
                        skill(cookingConceptIdentifier) {
                            description("Cooking for Dinner")
                            stillEnjoying(true)
                        }
                        skill(skateboardConceptIdentifier) {
                            description("Skateboarding")
                            stillEnjoying(false)
                        }
                    }

                // add some data in builder style
                val linda = builder
                    .newPerson(lindaConceptIdentifier, firstname = "Linda", sex = PersonSex.FEMALE)
                    .age(29)
                linda.skill(judoConceptIdentifier)
                    .description("Judo")
                    .stillEnjoying(true)

            }
        }

        val personList = schemaInstance.getPersonList()
        val skills = schemaInstance.getSkills()
        Assertions.assertEquals(2, personList.size)
        Assertions.assertEquals(3, skills.size)

        val james = personList.first { it.getConceptId() == jamesConceptIdentifier.getConceptId() }
        Assertions.assertEquals("James", james.getFirstname())
        Assertions.assertEquals(18, james.getAge())
        Assertions.assertEquals(PersonSex.MALE, james.getSex())


        val linda = personList.first { it.getConceptId() == lindaConceptIdentifier.getConceptId() }
        Assertions.assertEquals("Linda", linda.getFirstname())
        Assertions.assertEquals(29, linda.getAge())
        Assertions.assertEquals(PersonSex.FEMALE, linda.getSex())

        val judo = skills.first { it.getSkillConceptIdentifier() == judoConceptIdentifier.getConceptId().name }
        Assertions.assertEquals("Judo", judo.getSkillDescription())
        Assertions.assertEquals(true, judo.isStillFullyEnjoyingAboutThatSkill())

    }
}