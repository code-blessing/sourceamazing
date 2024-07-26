package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactory
import kotlin.reflect.KClass

object XmlTestSchemaCreator {

    val testEntityConceptName = TestEntityConcept::class.toConceptName()
    val testEntityNameFacetName = TestEntityConcept.Name::class.toFacetName()
    val testEntityKotlinModelClassnameFacetName = TestEntityConcept.KotlinModelClassname::class.toFacetName()
    val testEntityKotlinModelPackageFacetName = TestEntityConcept.KotlinModelPackage::class.toFacetName()
    val testEntityAttributeConceptName = TestEntityAttributeConcept::class.toConceptName()
    val testEntityAttributeNameFacetName = TestEntityAttributeConcept.Name::class.toFacetName()
    val testEntityAttributeTypeFacetName = TestEntityAttributeConcept.Type::class.toFacetName()


    fun createSchema(): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(XmlTestSchema::class)
    }

    private fun KClass<*>.toFacetName(): FacetName {
        return FacetName.of(MirrorFactory.convertToClassMirror(this))
    }

    private fun KClass<*>.toConceptName(): ConceptName {
        return ConceptName.of(MirrorFactory.convertToClassMirror(this))
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