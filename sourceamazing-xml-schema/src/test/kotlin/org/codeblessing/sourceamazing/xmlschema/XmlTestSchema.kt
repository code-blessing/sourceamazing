package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.FacetName
import org.codeblessing.sourceamazing.schema.api.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator

object XmlTestSchema {

    val testEntityConceptName = ConceptName.of(TestEntityConcept::class)
    val testEntityNameFacetName = FacetName.of("name")
    val testEntityKotlinModelClassnameFacetName = FacetName.of("kotlinModelClassname")
    val testEntityKotlinModelPackageFacetName = FacetName.of("kotlinModelPackage")
    val testEntityAttributeConceptName = ConceptName.of(TestEntityAttributeConcept::class)
    val testEntityAttributeNameFacetName = FacetName.of("name")
    val testEntityAttributeTypeFacetName = FacetName.of("type")


    fun createSchema(): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(XmlTestSchema::class)
    }

    interface XmlTestSchema {
        @Suppress("UNUSED")
        @Facet
        val testEntityConcepts: List<TestEntityConcept>
    }

    interface TestEntityConcept {
        @Suppress("UNUSED")
        @Facet
        val name: String

        @Suppress("UNUSED")
        @Facet
        val kotlinModelClassname: String

        @Suppress("UNUSED")
        @Facet
        val kotlinModelPackage: String

        @Suppress("UNUSED")
        @Facet
        val testEntityAttribute: List<TestEntityAttributeConcept>
    }

    interface TestEntityAttributeConcept {
        @Suppress("UNUSED")
        @Facet
        val name: String

        @Suppress("UNUSED")
        @Facet
        val type: AttributeTypeEnum

        enum class AttributeTypeEnum {
            @Suppress("UNUSED") TEXT,
            @Suppress("UNUSED") NUMBER,
            @Suppress("UNUSED") BOOLEAN,
        }
    }
}
