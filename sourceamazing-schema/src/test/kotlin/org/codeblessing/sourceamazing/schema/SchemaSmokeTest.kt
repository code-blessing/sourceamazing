package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SchemaSmokeTest {

    interface SmokeTestSchema {
        interface PersonConcept {

            enum class PersonSex { @Suppress("UNUSED") MALE, @Suppress("UNUSED") FEMALE }

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
