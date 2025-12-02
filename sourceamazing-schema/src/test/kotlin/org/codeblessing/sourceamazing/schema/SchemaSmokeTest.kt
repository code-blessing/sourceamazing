package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("UNUSED", "Unused")
class SchemaSmokeTest {

    interface SmokeTestSchema {
        interface PersonConcept {

            enum class PersonSex {
                MALE,
                FEMALE,
            }

            val firstname: String

            val age: Int

            val sex: PersonSex
        }

        interface SkillConcept {

            val skillConceptIdentifier: String

            val skillDescription: String

            val isStillFullyEnjoyingAboutThatSkill: Boolean
        }

        val personList: List<PersonConcept>

        val skills: List<SkillConcept>
    }

    @Test
    fun `test sourceamazing schema as smoke test`() {
        val schemaInstance: SmokeTestSchema =
            SchemaApi.withSchema(schemaDefinitionClass = SmokeTestSchema::class) { schemaContext ->
                schemaContext.withDefaultValueRootInstance<SmokeTestSchema> {
                    // do nothing
                }
            }

        Assertions.assertEquals(0, schemaInstance.personList.size)
        Assertions.assertEquals(0, schemaInstance.skills.size)
    }
}
