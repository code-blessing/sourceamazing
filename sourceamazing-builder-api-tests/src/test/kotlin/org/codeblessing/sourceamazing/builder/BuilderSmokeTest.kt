package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderSmokeTest.SmokeTestSchema.PersonClazz.PersonSex
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.api.by
import org.codeblessing.sourceamazing.schema.UniqueId
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderSmokeTest {

    interface SmokeTestSchema {
        interface PersonClazz {

            enum class PersonSex {
                MALE,
                FEMALE,
            }

            val firstname: String

            val age: Int

            val sex: PersonSex

            val skills: List<SkillClazz>
        }

        interface SkillClazz {

            val skillDescription: String

            val isStillFullyEnjoyingAboutThatSkill: Boolean
        }

        val personList: List<PersonClazz>
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = SmokeTestSchema::class, alias = "root")
    interface SmokeTestRootBuilder {

        // Builder style
        @BuilderMethod
        @NewClazzModel(clazz = SmokeTestSchema.PersonClazz::class, alias = "person")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "personList", referencedAlias = "person")
        fun newPerson(
            @SetAsClazzModelId(alias = "person") clazzModelId: UniqueId,
            @SetAsValue(alias = "person", clazzProperty = "firstname") firstname: String,
            @SetAsValue(alias = "person", clazzProperty = "sex") sex: PersonSex,
        ): PersonClazzBuilder

        fun newPerson(clazzModelId: PersonClazzModelId, firstname: String, sex: PersonSex): PersonClazzBuilder {
            return newPerson(clazzModelId.getClazzModelId(), firstname, sex)
        }

        // DSL style
        @BuilderMethod
        @NewClazzModel(clazz = SmokeTestSchema.PersonClazz::class, alias = "person")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "personList", referencedAlias = "person")
        fun newPerson(
            @SetAsClazzModelId(alias = "person") clazzModelId: UniqueId,
            @InjectBuilder builder: PersonClazzBuilder.() -> Unit,
        )

        fun newPerson(clazzModelId: PersonClazzModelId, builder: PersonClazzBuilder.() -> Unit) {
            newPerson(clazzModelId.getClazzModelId(), builder)
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = SmokeTestSchema.PersonClazz::class, alias = "person")
        interface PersonClazzBuilder {

            @BuilderMethod
            fun firstname(
                @SetAsValue(alias = "person", clazzProperty = "firstname") firstname: String
            ): PersonClazzBuilder

            @BuilderMethod fun age(@SetAsValue(alias = "person", clazzProperty = "age") age: Int): PersonClazzBuilder

            @BuilderMethod
            fun sex(@SetAsValue(alias = "person", clazzProperty = "sex") sex: PersonSex): PersonClazzBuilder

            @BuilderMethod
            fun firstnameAndAge(
                @SetAsValue(alias = "person", clazzProperty = "firstname") firstname: String,
                @SetAsValue(alias = "person", clazzProperty = "age") age: Int,
            ): PersonClazzBuilder

            // Builder style
            @BuilderMethod
            @NewClazzModel(SmokeTestSchema.SkillClazz::class, alias = "skill")
            @SetClazzModelOfAlias(alias = "person", clazzProperty = "skills", referencedAlias = "skill")
            fun skill(@SetAsClazzModelId(alias = "skill") skillClazzModelId: UniqueId): SkillClazzBuilder

            fun skill(skillClazzModelId: SkillClazzModelId): SkillClazzBuilder {
                return skill(skillClazzModelId.getClazzModelId())
            }

            // DSL style
            @BuilderMethod
            @NewClazzModel(SmokeTestSchema.SkillClazz::class, alias = "skill")
            @SetClazzModelOfAlias(alias = "person", clazzProperty = "skills", referencedAlias = "skill")
            fun skill(
                @SetAsClazzModelId(alias = "skill") skillClazzModelId: UniqueId,
                @InjectBuilder builder: SkillClazzBuilder.() -> Unit,
            )

            fun skill(skillClazzModelId: SkillClazzModelId, builder: SkillClazzBuilder.() -> Unit)
        }

        @Builder
        @ExpectedClazzModelFromSuperiorBuilder(clazz = SmokeTestSchema.SkillClazz::class, alias = "skill")
        interface SkillClazzBuilder {

            @BuilderMethod
            fun descriptionAndStillEnjoying(
                @SetAsValue(alias = "skill", clazzProperty = "skillDescription") description: String,
                @SetAsValue(alias = "skill", clazzProperty = "isStillFullyEnjoyingAboutThatSkill")
                stillEnjoying: Boolean,
            ): SkillClazzBuilder

            @BuilderMethod
            fun description(
                @SetAsValue(alias = "skill", clazzProperty = "skillDescription") description: String
            ): SkillClazzBuilder

            @BuilderMethod
            fun stillEnjoying(
                @SetAsValue(alias = "skill", clazzProperty = "isStillFullyEnjoyingAboutThatSkill")
                stillEnjoying: Boolean
            ): SkillClazzBuilder
        }
    }

    class PersonClazzBuilderImpl(private val proxyBuilder: SmokeTestRootBuilder.PersonClazzBuilder) :
        SmokeTestRootBuilder.PersonClazzBuilder by proxyBuilder {

        override fun skill(
            skillClazzModelId: SkillClazzModelId,
            builder: SmokeTestRootBuilder.SkillClazzBuilder.() -> Unit,
        ) {
            return proxyBuilder.skill(skillClazzModelId.getClazzModelId(), builder)
        }
    }

    class PersonClazzModelId(val clazzModelId: String) {
        fun getClazzModelId() = UniqueId.of(clazzModelId)
    }

    class SkillClazzModelId(val clazzModelId: String) {
        fun getClazzModelId() = UniqueId.of(clazzModelId)
    }

    @Test
    fun `test sourceamazing builder as smoke test`() {
        val jamesClazzModelId = PersonClazzModelId("James")
        val cookingClazzModelId = SkillClazzModelId("Cooking")
        val skateboardClazzModelId = SkillClazzModelId("Skateboard")
        val lindaClazzModelId = PersonClazzModelId("Linda")
        val judoClazzModelId = SkillClazzModelId("Judo")

        val schemaInstance: SmokeTestSchema =
            SchemaApi.withSchema(SmokeTestSchema::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext = schemaContext,
                    builderClass = SmokeTestRootBuilder::class,
                    builderFactories =
                        setOf(SmokeTestRootBuilder.PersonClazzBuilder::class by PersonClazzBuilderImpl::class),
                ) { builder ->
                    // add some data in DSL style
                    builder.newPerson(jamesClazzModelId) {
                        firstnameAndAge(firstname = "James", age = 18)
                        sex(PersonSex.MALE)
                        skill(cookingClazzModelId) {
                            description("Cooking for Dinner")
                            stillEnjoying(true)
                        }
                        skill(skateboardClazzModelId) {
                            description("Skateboarding")
                            stillEnjoying(false)
                        }
                    }

                    // add some data in builder style
                    val linda =
                        builder.newPerson(lindaClazzModelId, firstname = "Linda", sex = PersonSex.FEMALE).age(29)
                    linda.skill(judoClazzModelId).description("Judo").stillEnjoying(true)
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
