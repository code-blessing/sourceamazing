package org.codeblessing.sourceamazing.schema

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

class SchemaSmokeTest {

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

            enum class PersonSex { @Suppress("UNUSED") MALE, @Suppress("UNUSED") FEMALE }

            @StringFacet()
            interface PersonFirstnameFacet
            @IntFacet
            interface PersonAgeFacet
            @EnumFacet(enumerationClass = PersonSex::class)
            interface PersonSexFacet

            @Suppress("UNUSED")
            @QueryConceptIdentifierValue
            fun getConceptId(): ConceptIdentifier

            @Suppress("UNUSED")
            @QueryFacetValue(PersonFirstnameFacet::class)
            fun getFirstname(): String

            @Suppress("UNUSED")
            @QueryFacetValue(PersonAgeFacet::class)
            fun getAge(): Int

            @Suppress("UNUSED")
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

            @Suppress("UNUSED")
            @QueryConceptIdentifierValue
            fun getSkillConceptIdentifier(): String

            @Suppress("UNUSED")
            @QueryFacetValue(SkillDescriptionFacet::class)
            fun getSkillDescription(): String

            @Suppress("UNUSED")
            @QueryFacetValue(SkillStillEnjoyingFacet::class)
            fun isStillFullyEnjoyingAboutThatSkill(): Boolean

        }

        @QueryConcepts(conceptClasses = [PersonConcept::class])
        fun getPersonList(): List<PersonConcept>

        @QueryConcepts(conceptClasses = [SkillConcept::class])
        fun getSkills(): List<SkillConcept>

    }

    @Test
    fun `test sourceamazing schema as smoke test`() {
        val schemaInstance: SmokeTestSchema = SchemaApi.withSchema(schemaDefinitionClass = SmokeTestSchema::class) {
            // do nothing
        }

        Assertions.assertEquals(0, schemaInstance.getPersonList().size)
        Assertions.assertEquals(0, schemaInstance.getSkills().size)
    }
}