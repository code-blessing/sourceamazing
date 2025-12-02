package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderSmokeTest.SmokeTestSchema.PersonConcept.PersonSex
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.toConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.withDefaultValueRootInstance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderSmokeTest {

    interface SmokeTestSchema {
        interface PersonConcept {

            enum class PersonSex {
                MALE,
                FEMALE,
            }

            val firstname: String

            val age: Int

            val sex: PersonSex

            val skills: List<SkillConcept>
        }

        interface SkillConcept {

            val skillDescription: String

            val isStillFullyEnjoyingAboutThatSkill: Boolean
        }

        val personList: List<PersonConcept>
    }

    @Builder
    @ExpectedAliasFromSuperiorBuilder(concept = SmokeTestSchema::class, conceptAlias = "root")
    interface SmokeTestRootBuilder {

        // Builder style
        @BuilderMethod
        @NewConcept(concept = SmokeTestSchema.PersonConcept::class, declareConceptAlias = "person")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "personList",
            referencedConceptAlias = "person",
        )
        fun newPerson(
            @ProvideBuilderData conceptIdentifier: PersonConceptIdentifier,
            @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "firstname") firstname: String,
            @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "sex") sex: PersonSex,
        ): PersonConceptBuilder

        // DSL style
        @BuilderMethod
        @NewConcept(concept = SmokeTestSchema.PersonConcept::class, declareConceptAlias = "person")
        @SetAliasConceptIdentifierReferenceFacetValue(
            conceptToModifyAlias = "root",
            facetToModify = "personList",
            referencedConceptAlias = "person",
        )
        fun newPerson(
            @ProvideBuilderData conceptIdentifier: PersonConceptIdentifier,
            @InjectBuilder builder: PersonConceptBuilder.() -> Unit,
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = SmokeTestSchema.PersonConcept::class, conceptAlias = "person")
        interface PersonConceptBuilder {

            @BuilderMethod
            fun firstname(
                @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "firstname") firstname: String
            ): PersonConceptBuilder

            @BuilderMethod
            fun age(
                @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "age") age: Int
            ): PersonConceptBuilder

            @BuilderMethod
            fun sex(
                @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "sex") sex: PersonSex
            ): PersonConceptBuilder

            @BuilderMethod
            fun firstnameAndAge(
                @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "firstname") firstname: String,
                @SetFacetValue(conceptToModifyAlias = "person", facetToModify = "age") age: Int,
            ): PersonConceptBuilder

            // Builder style
            @BuilderMethod
            @NewConcept(SmokeTestSchema.SkillConcept::class, declareConceptAlias = "skill")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "person",
                facetToModify = "skills",
                referencedConceptAlias = "skill",
            )
            fun skill(@ProvideBuilderData skillConceptIdentifier: SkillConceptIdentifier): SkillConceptBuilder

            // DSL style
            @BuilderMethod
            @NewConcept(SmokeTestSchema.SkillConcept::class, declareConceptAlias = "skill")
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "person",
                facetToModify = "skills",
                referencedConceptAlias = "skill",
            )
            fun skill(
                @ProvideBuilderData skillConceptIdentifier: SkillConceptIdentifier,
                @InjectBuilder builder: SkillConceptBuilder.() -> Unit,
            )
        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = SmokeTestSchema.SkillConcept::class, conceptAlias = "skill")
        interface SkillConceptBuilder {

            @BuilderMethod
            fun descriptionAndStillEnjoying(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "skillDescription") description: String,
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "isStillFullyEnjoyingAboutThatSkill")
                stillEnjoying: Boolean,
            ): SkillConceptBuilder

            @BuilderMethod
            fun description(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "skillDescription") description: String
            ): SkillConceptBuilder

            @BuilderMethod
            fun stillEnjoying(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "isStillFullyEnjoyingAboutThatSkill")
                stillEnjoying: Boolean
            ): SkillConceptBuilder
        }
    }

    @BuilderDataProvider
    class PersonConceptIdentifier(private val conceptIdentifier: String) {

        @BuilderData
        @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "person")
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

        val schemaInstance: SmokeTestSchema =
            SchemaApi.withSchema(SmokeTestSchema::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<SmokeTestSchema> { conceptData ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        SmokeTestRootBuilder::class,
                        mapOf("root" to conceptData.toConceptNameAndIdentifier()),
                    ) { builder ->
                        // add some data in DSL style
                        builder.newPerson(jamesConceptIdentifier) {
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
                        val linda =
                            builder
                                .newPerson(lindaConceptIdentifier, firstname = "Linda", sex = PersonSex.FEMALE)
                                .age(29)
                        linda.skill(judoConceptIdentifier).description("Judo").stillEnjoying(true)
                    }
                }
            }

        val personList = schemaInstance.personList
        Assertions.assertEquals(2, personList.size)

        val james = personList.first { it.firstname == "James" }
        Assertions.assertEquals("James", james.firstname)
        Assertions.assertEquals(18, james.age)
        Assertions.assertEquals(PersonSex.MALE, james.sex)
        Assertions.assertEquals(2, james.skills.size)

        val linda = personList.first { it.firstname == "Linda" }
        Assertions.assertEquals("Linda", linda.firstname)
        Assertions.assertEquals(29, linda.age)
        Assertions.assertEquals(PersonSex.FEMALE, linda.sex)
        Assertions.assertEquals(1, linda.skills.size)
        val skillsOfLinda = linda.skills
        val judo = skillsOfLinda.first { it.skillDescription == "Judo" }
        Assertions.assertEquals("Judo", judo.skillDescription)
        Assertions.assertEquals(true, judo.isStillFullyEnjoyingAboutThatSkill)
    }
}
