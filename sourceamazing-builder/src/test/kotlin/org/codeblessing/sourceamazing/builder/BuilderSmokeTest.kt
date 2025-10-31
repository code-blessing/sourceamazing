package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderSmokeTest.SmokeTestSchema.PersonConcept.PersonSex
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BuilderSmokeTest {

    interface SmokeTestSchema {
        interface PersonConcept {

            enum class PersonSex { @Suppress("UNUSED") MALE, @Suppress("UNUSED") FEMALE }

            @Suppress("UNUSED")
            @Facet
            val personId: String

            @Suppress("UNUSED")
            @Facet
            val firstname: String

            @Suppress("UNUSED")
            @Facet
            val age: Int

            @Suppress("UNUSED")
            @Facet
            val sex: PersonSex

        }
        interface SkillConcept {

            @Suppress("UNUSED")
            @Facet
            val skillConceptIdentifier: String

            @Suppress("UNUSED")
            @Facet
            val skillDescription: String

            @Suppress("UNUSED")
            @Facet
            val isStillFullyEnjoyingAboutThatSkill: Boolean

        }

        @Facet
        val personList: List<PersonConcept>

        @Facet
        val skills: List<SkillConcept>

    }

    @Builder
    interface SmokeTestRootBuilder {

        // Builder style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = SmokeTestSchema.PersonConcept::class)
        fun newPerson(
            @ProvideBuilderData conceptIdentifier: PersonConceptIdentifier,
            @SetFacetValue(facetToModify = "firstname") firstname: String,
            @SetFacetValue(facetToModify = "sex") sex: PersonSex,
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
                @SetFacetValue(facetToModify = "firstname") firstname: String,
            ): PersonConceptBuilder

            @BuilderMethod
            fun age(
                @SetFacetValue(facetToModify = "age")  age: Int,
            ): PersonConceptBuilder

            @BuilderMethod
            fun sex(
                @SetFacetValue(facetToModify = "sex")  sex: PersonSex,
            ): PersonConceptBuilder

            @BuilderMethod
            fun firstnameAndAge(
                @SetFacetValue(facetToModify = "firstname") firstname: String,
                @SetFacetValue(facetToModify = "age")  age: Int,
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
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "skillDescription") description: String,
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "isStillFullyEnjoyingAboutThatSkill") stillEnjoying: Boolean,
            ): SkillConceptBuilder

            @BuilderMethod
            fun description(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "skillDescription") description: String,
            ): SkillConceptBuilder

            @BuilderMethod
            fun stillEnjoying(
                @SetFacetValue(conceptToModifyAlias = "skill", facetToModify = "isStillFullyEnjoyingAboutThatSkill") stillEnjoying: Boolean,
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


        val schemaInstance: SmokeTestSchema = SchemaApi.withSchema(SmokeTestSchema::class) { schemaContext -> 
            withRootInstance<SmokeTestSchema>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    rootConceptIdentifier,
                    SmokeTestRootBuilder::class
                ) { builder ->
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
        }

        val personList = schemaInstance.personList
        val skills = schemaInstance.skills
        Assertions.assertEquals(2, personList.size)
        Assertions.assertEquals(3, skills.size)

        val james = personList.first { it.personId == jamesConceptIdentifier.getConceptId().name }
        Assertions.assertEquals("James", james.firstname)
        Assertions.assertEquals(18, james.age)
        Assertions.assertEquals(PersonSex.MALE, james.sex)


        val linda = personList.first { it.personId == lindaConceptIdentifier.getConceptId().name }
        Assertions.assertEquals("Linda", linda.firstname)
        Assertions.assertEquals(29, linda.age)
        Assertions.assertEquals(PersonSex.FEMALE, linda.sex)

        val judo = skills.first { it.skillConceptIdentifier == judoConceptIdentifier.getConceptId().name }
        Assertions.assertEquals("Judo", judo.skillDescription)
        Assertions.assertEquals(true, judo.isStillFullyEnjoyingAboutThatSkill)

    }
}
