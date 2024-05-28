package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderSmokeTest.SmokeTestSchema.PersonConcept.PersonSex
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.*
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
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
    interface SmokeTestDataCollectorRootBuilder {

        // Builder style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = SmokeTestSchema.PersonConcept::class)
        fun newPerson(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonFirstnameFacet::class) firstname: String,
            @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonSexFacet::class) sex: PersonSex,
        ): PersonConceptBuilder

        // DSL style
        @BuilderMethod
        @WithNewBuilder(PersonConceptBuilder::class)
        @NewConcept(concept = SmokeTestSchema.PersonConcept::class)
        fun newPerson(
            @SetConceptIdentifierValue conceptIdentifier: ConceptIdentifier,
            @InjectBuilder builder: PersonConceptBuilder.() -> Unit,
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder
        interface PersonConceptBuilder {

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
                @SetFacetValue(facetToModify = SmokeTestSchema.PersonConcept.PersonSexFacet::class)  sex: String,
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
                @SetConceptIdentifierValue(conceptToModifyAlias = "skill") skillConceptIdentifier: ConceptIdentifier,
            ): SkillConceptBuilder

            // DSL style
            @BuilderMethod
            @WithNewBuilder(builderClass = SkillConceptBuilder::class)
            @NewConcept(SmokeTestSchema.SkillConcept::class, declareConceptAlias = "skill")
            fun skill(
                @SetConceptIdentifierValue(conceptToModifyAlias = "skill") skillConceptIdentifier: ConceptIdentifier,
                @InjectBuilder builder: SkillConceptBuilder.() -> Unit,
            )

        }

        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "skill")
        interface SkillConceptBuilder {

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

    @Test
    fun `test sourceamazing builder as smoke test`() {
        val jamesConceptIdentifier = ConceptIdentifier.of("James")
        val cookingConceptIdentifier = ConceptIdentifier.of("Cooking")
        val skateboardConceptIdentifier = ConceptIdentifier.of("Skateboard")
        val lindaConceptIdentifier = ConceptIdentifier.of("Linda")
        val judoConceptIdentifier = ConceptIdentifier.of("Judo")


        val schemaInstance: SmokeTestSchema = SchemaApi.withSchema(schemaDefinitionClass = SmokeTestSchema::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, SmokeTestDataCollectorRootBuilder::class) { dataCollector ->
                // add some data in DSL style
                dataCollector
                    .newPerson(jamesConceptIdentifier) {
                        firstnameAndAge(firstname = "James", age = 18)
                        sex(PersonSex.MALE.toString())
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
                val linda = dataCollector
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

        val james = personList.first { it.getConceptId() == jamesConceptIdentifier }
        Assertions.assertEquals("James", james.getFirstname())
        Assertions.assertEquals(18, james.getAge())
        Assertions.assertEquals(PersonSex.MALE, james.getSex())


        val linda = personList.first { it.getConceptId() == lindaConceptIdentifier }
        Assertions.assertEquals("Linda", linda.getFirstname())
        Assertions.assertEquals(29, linda.getAge())
        Assertions.assertEquals(PersonSex.FEMALE, linda.getSex())

        val judo = skills.first { it.getSkillConceptIdentifier() == judoConceptIdentifier.name }
        Assertions.assertEquals("Judo", judo.getSkillDescription())
        Assertions.assertEquals(true, judo.isStillFullyEnjoyingAboutThatSkill())

    }
}