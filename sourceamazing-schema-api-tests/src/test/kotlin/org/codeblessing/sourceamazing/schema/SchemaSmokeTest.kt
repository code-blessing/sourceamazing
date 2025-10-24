package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.SchemaSmokeTest.PersonGender.MALE
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class SchemaSmokeTest {

    data class PersonAndSkills(val personList: List<PersonClazz>)

    interface PersonClazz {
        val firstname: String
        val age: Int
        val gender: PersonGender
        val skills: List<SkillClazz>
    }

    enum class PersonGender {
        MALE,
        FEMALE,
    }

    data class SkillClazz(val description: String, val isSillPracticing: Boolean)

    @Test
    fun `test sourceamazing schema as smoke test`() {
        val schemaInstance: PersonAndSkills =
            SchemaApi.withSchema(rootClazz = PersonAndSkills::class) { schemaContext ->
                val donaldsMainSkill =
                    schemaContext.dataCollector
                        .newClazzModel(SkillClazz::class)
                        .addClazzPropertyValue("description", "chatter and quack")
                        .addClazzPropertyValue("isSillPracticing", true)

                val donaldDuck =
                    schemaContext.dataCollector
                        .newClazzModel(PersonClazz::class)
                        .addClazzPropertyValue("firstname", "Donald")
                        .addClazzPropertyValue("age", 42)
                        .addClazzPropertyValue("gender", MALE)
                        .addClazzPropertyReference("skills", donaldsMainSkill)

                schemaContext.dataCollector.rootClazzModel().addClazzPropertyReference("personList", donaldDuck)
            }

        Assertions.assertEquals(1, schemaInstance.personList.size)
        val donaldDuck = schemaInstance.personList.first()
        Assertions.assertEquals("Donald", donaldDuck.firstname)
        Assertions.assertEquals(42, donaldDuck.age)
        Assertions.assertEquals(MALE, donaldDuck.gender)
        Assertions.assertEquals(1, donaldDuck.skills.size)
        val donaldsOnlySkill = donaldDuck.skills.first()
        Assertions.assertEquals(true, donaldsOnlySkill.isSillPracticing)
        Assertions.assertEquals("chatter and quack", donaldsOnlySkill.description)
    }
}
