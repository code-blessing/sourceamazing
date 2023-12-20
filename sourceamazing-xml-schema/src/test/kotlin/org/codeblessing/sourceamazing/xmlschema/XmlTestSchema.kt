package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.engine.process.schema.SchemaCreator

object XmlTestSchema {

    val testEntityConceptName = ConceptName.of(TestEntityConcept::class)
    val testEntityNameFacetName = FacetName.of(TestEntityConcept.Name::class)
    val testEntityKotlinModelClassnameFacetName = FacetName.of(TestEntityConcept.KotlinModelClassname::class)
    val testEntityKotlinModelPackageFacetName = FacetName.of(TestEntityConcept.KotlinModelPackage::class)
    val testEntityAttributeConceptName = ConceptName.of(TestEntityAttributeConcept::class)
    val testEntityAttributeNameFacetName = FacetName.of(TestEntityAttributeConcept.Name::class)
    val testEntityAttributeTypeFacetName = FacetName.of(TestEntityAttributeConcept.Type::class)


    fun createSchema(): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(XmlTestSchema::class)
    }


    @Schema(concepts = [
        TestEntityConcept::class,
        TestEntityAttributeConcept::class,
    ])
    interface XmlTestSchema

    @Concept(facets = [
        TestEntityConcept.Name::class,
        TestEntityConcept.KotlinModelClassname::class,
        TestEntityConcept.KotlinModelPackage::class,
        TestEntityConcept.TestEntityAttribute::class,
    ])
    interface TestEntityConcept {
        @StringFacet
        interface Name

        @StringFacet
        interface KotlinModelClassname

        @StringFacet
        interface KotlinModelPackage

        @ReferenceFacet(minimumOccurrences = 0, maximumOccurrences = 10, referencedConcepts = [TestEntityAttributeConcept::class])
        interface TestEntityAttribute
    }

    @Concept(facets = [
        TestEntityAttributeConcept.Name::class,
        TestEntityAttributeConcept.Type::class,
    ])
    interface TestEntityAttributeConcept {
        @StringFacet
        interface Name

        @EnumFacet(enumerationClass = AttributeTypeEnum::class)
        interface Type

        enum class AttributeTypeEnum {
            TEXT,
            NUMBER,
            BOOLEAN,
        }
    }
}