package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaSmokeTest {

    interface SmokeTestSchema {
        interface PersonConcept {

            enum class PersonSex { @Suppress("UNUSED") MALE, @Suppress("UNUSED") FEMALE }

            @Suppress("UNUSED")
            val firstname: String

            @Suppress("UNUSED")
            val age: Int

            @Suppress("UNUSED")
            val sex: PersonSex

        }
        interface SkillConcept {

            @Suppress("UNUSED")
            val skillConceptIdentifier: String

            @Suppress("UNUSED")
            val skillDescription: String

            @Suppress("UNUSED")
            val isStillFullyEnjoyingAboutThatSkill: Boolean

        }

        val personList: List<PersonConcept>

        val skills: List<SkillConcept>

    }

    @Test
    fun `test sourceamazing schema as smoke test`() {
        val schemaInstance: SmokeTestSchema = SchemaApi.withSchema(schemaDefinitionClass = SmokeTestSchema::class) { schemaContext ->
            withRootInstance<SmokeTestSchema>(schemaContext) {
                // do nothing
            }

        }

        Assertions.assertEquals(0, schemaInstance.personList.size)
        Assertions.assertEquals(0, schemaInstance.skills.size)
    }
}
