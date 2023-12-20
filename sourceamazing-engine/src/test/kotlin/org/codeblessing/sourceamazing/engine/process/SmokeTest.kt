package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.api.process.datacollection.builder.annotations.*
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConceptId
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryConcepts
import org.codeblessing.sourceamazing.api.process.schema.query.annotations.QueryFacet
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import org.codeblessing.sourceamazing.engine.process.SmokeTest.SmokeTestSchema.PersonConcept.PersonSex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SmokeTest {

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
            @IntFacet interface PersonAgeFacet
            @EnumFacet(enumerationClass = PersonSex::class) interface PersonSexFacet

            @QueryConceptId
            fun getConceptId(): ConceptIdentifier

            @QueryFacet(PersonFirstnameFacet::class)
            fun getFirstname(): String

            @QueryFacet(PersonAgeFacet::class)
            fun getAge(): Int

            @QueryFacet(PersonSexFacet::class)
            fun getSex(): PersonSex

        }
        @Concept(facets = [
            SkillConcept.SkillDescriptionFacet::class,
            SkillConcept.SkillStillEnjoyingFacet::class,
        ])
        interface SkillConcept {
            @StringFacet() interface SkillDescriptionFacet
            @BooleanFacet() interface SkillStillEnjoyingFacet

            @QueryConceptId
            fun getSkillConceptIdentifier(): String

            @QueryFacet(SkillDescriptionFacet::class)
            fun getSkillDescription(): String

            @QueryFacet(SkillStillEnjoyingFacet::class)
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


    private class SmokeTestDomainUnit: DomainUnit<SmokeTestSchema, SmokeTestDataCollectorRootBuilder>(
        schemaDefinitionClass = SmokeTestSchema::class,
        inputDefinitionClass = SmokeTestDataCollectorRootBuilder::class
    ) {
        private val jamesConceptIdentifier = ConceptIdentifier.of("James")
        private val cookingConceptIdentifier = ConceptIdentifier.of("Cooking")
        private val skateboardConceptIdentifier = ConceptIdentifier.of("Skateboard")
        private val lindaConceptIdentifier = ConceptIdentifier.of("Linda")
        private val judoConceptIdentifier = ConceptIdentifier.of("Judo")

        override fun collectInputData(
            parameterAccess: ParameterAccess,
            extensionAccess: DataCollectionExtensionAccess,
            dataCollector: SmokeTestDataCollectorRootBuilder
        ) {

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

        override fun collectTargetFiles(
            parameterAccess: ParameterAccess,
            schemaInstance: SmokeTestSchema,
            targetFilesCollector: TargetFilesCollector
        ) {

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

    @Test
    fun `test sourceamazing main process as smoke test`() {
        val testProcessSession = ProcessSession(domainUnits = listOf(SmokeTestDomainUnit()))
        val engineProcess = EngineProcess(testProcessSession)

        engineProcess.runProcess()
    }
}