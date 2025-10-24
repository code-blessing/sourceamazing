package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreatorTest.SchemaWithClazzWithEmptyEnumClazzProperty.EmptyEnumeration
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz
import org.codeblessing.sourceamazing.schema.utils.type.enumValues
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@Suppress("UNUSED")
class SchemaCreatorTest {

    private interface EmptySchemaDefinitionClass

    @Test
    fun `test create an empty schema from an empty schema interface`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(EmptySchemaDefinitionClass::class)
        assertEquals(1, schema.numberOfClazzes())
    }

    private interface SchemaWithEmptyClazzClass {

        interface EmptyClazzClass

        val oneClazz: EmptyClazzClass
    }

    @Test
    fun `test create an schema with an empty clazz class`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithEmptyClazzClass::class)
        assertEquals(2, schema.numberOfClazzes())
    }

    private interface SchemaWithClazzWithTextClazzPropertyClass {
        val myText: String
    }

    @Test
    fun `test create an schema with clazz class having a text clazzProperty`() {
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithClazzWithTextClazzPropertyClass::class)
        assertEquals(2, schema.numberOfClazzes())
        val clazz =
            requireNotNull(schema.clazzSchemaByClazz(Clazz.of(SchemaWithClazzWithTextClazzPropertyClass::class)))
        assertNotNull(
            clazz.clazzPropertyByName(ClassProperty.of(SchemaWithClazzWithTextClazzPropertyClass::myText.name))
        )
    }

    private interface SchemaWithClazzWithCorrectCardinalityClazzProperties {
        val myText: String?

        val myBoolean: Boolean

        val myNumbers: Set<Int>
    }

    @Test
    fun `test clazz having three clazzProperties with correct cardinalities`() {
        val myTextClassProperty = ClassProperty.of(SchemaWithClazzWithCorrectCardinalityClazzProperties::myText.name)
        val myBooleanClassProperty =
            ClassProperty.of(SchemaWithClazzWithCorrectCardinalityClazzProperties::myBoolean.name)
        val myNumbersClassProperty =
            ClassProperty.of(SchemaWithClazzWithCorrectCardinalityClazzProperties::myNumbers.name)
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(
                SchemaWithClazzWithCorrectCardinalityClazzProperties::class
            )
        val clazzSchema =
            requireNotNull(
                schema.clazzSchemaByClazz(Clazz.of(SchemaWithClazzWithCorrectCardinalityClazzProperties::class))
            )

        assertEquals(0, clazzSchema.clazzPropertyByName(myTextClassProperty)?.minimumOccurrences)
        assertEquals(1, clazzSchema.clazzPropertyByName(myTextClassProperty)?.maximumOccurrences)

        assertEquals(1, clazzSchema.clazzPropertyByName(myBooleanClassProperty)?.minimumOccurrences)
        assertEquals(1, clazzSchema.clazzPropertyByName(myBooleanClassProperty)?.maximumOccurrences)

        assertEquals(0, clazzSchema.clazzPropertyByName(myNumbersClassProperty)?.minimumOccurrences)
        assertEquals(Int.MAX_VALUE, clazzSchema.clazzPropertyByName(myNumbersClassProperty)?.maximumOccurrences)
    }

    private interface SchemaWithClazzWithEmptyEnumClazzProperty {
        enum class EmptyEnumeration

        val myEnum: EmptyEnumeration
    }

    @Test
    fun `test clazz having an empty enumeration clazzProperty should not throw an exception`() {
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithClazzWithEmptyEnumClazzProperty::class)

        val clazzSchema =
            requireNotNull(schema.clazzSchemaByClazz(Clazz.of(SchemaWithClazzWithEmptyEnumClazzProperty::class)))
        val enumClassProperty = ClassProperty.of(SchemaWithClazzWithEmptyEnumClazzProperty::myEnum.name)
        val enumClazzPropertySchema =
            clazzSchema.clazzPropertyByName(enumClassProperty) ?: fail("ClazzProperty '$enumClassProperty' not found")
        assertEquals(enumClassProperty, enumClazzPropertySchema.classProperty)
        assertEquals(SchemaWithClazzWithEmptyEnumClazzProperty::class.toClazz(), enumClazzPropertySchema.enclosingClazz)
        assertEquals(EmptyEnumeration::class.toClazz(), enumClazzPropertySchema.clazzPropertyClazz)
        assertEquals(0, enumClazzPropertySchema.clazzPropertyClazz.clazz.enumValues.size)
    }

    private interface SchemaWithClazzWithPrimitiveClazzPropertyClasses {
        val myText: String

        val myBoolean: Boolean

        val myNumber: Int
    }

    @Test
    fun `test clazz having three primitive type clazzProperty`() {
        val schema =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithClazzWithPrimitiveClazzPropertyClasses::class)
        assertEquals(4, schema.numberOfClazzes())
        val clazzSchema =
            requireNotNull(schema.clazzSchemaByClazz(Clazz.of(SchemaWithClazzWithPrimitiveClazzPropertyClasses::class)))
        assertEquals(3, clazzSchema.clazzProperties.size)

        val textClassProperty = ClassProperty.of(SchemaWithClazzWithPrimitiveClazzPropertyClasses::myText.name)
        val booleanClassProperty = ClassProperty.of(SchemaWithClazzWithPrimitiveClazzPropertyClasses::myBoolean.name)
        val numberClassProperty = ClassProperty.of(SchemaWithClazzWithPrimitiveClazzPropertyClasses::myNumber.name)

        assertEquals(textClassProperty, clazzSchema.clazzPropertyByName(textClassProperty)?.classProperty)
        assertEquals(booleanClassProperty, clazzSchema.clazzPropertyByName(booleanClassProperty)?.classProperty)
        assertEquals(numberClassProperty, clazzSchema.clazzPropertyByName(numberClassProperty)?.classProperty)

        assertEquals(String::class.toClazz(), clazzSchema.clazzPropertyByName(textClassProperty)?.clazzPropertyClazz)
        assertEquals(
            Boolean::class.toClazz(),
            clazzSchema.clazzPropertyByName(booleanClassProperty)?.clazzPropertyClazz,
        )
        assertEquals(Int::class.toClazz(), clazzSchema.clazzPropertyByName(numberClassProperty)?.clazzPropertyClazz)
    }

    private interface SchemaWithClazzWithEnumClazzProperty {
        val mySeasonEnum: SeasonEnumeration

        enum class SeasonEnumeration {
            WINTER,
            SPRING,
            SUMMER,
            FALL,
        }
    }

    @Test
    fun `test clazz having a enumeration clazzProperty`() {
        val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaWithClazzWithEnumClazzProperty::class)

        val clazzSchema =
            requireNotNull(schema.clazzSchemaByClazz(Clazz.of(SchemaWithClazzWithEnumClazzProperty::class)))
        val enumClassProperty = ClassProperty.of(SchemaWithClazzWithEnumClazzProperty::mySeasonEnum.name)
        val enumClazzPropertySchema =
            clazzSchema.clazzPropertyByName(enumClassProperty) ?: fail("ClazzProperty '$enumClassProperty' not found")
        assertEquals(enumClassProperty, enumClazzPropertySchema.classProperty)
        assertEquals(SchemaWithClazzWithEnumClazzProperty::class.toClazz(), enumClazzPropertySchema.enclosingClazz)
        assertEquals(
            SchemaWithClazzWithEnumClazzProperty.SeasonEnumeration::class.toClazz(),
            enumClazzPropertySchema.clazzPropertyClazz,
        )
        assertEquals(4, enumClazzPropertySchema.clazzPropertyClazz.clazz.enumValues.size)
        assertEquals(
            SchemaWithClazzWithEnumClazzProperty.SeasonEnumeration.WINTER,
            enumClazzPropertySchema.clazzPropertyClazz.clazz.enumValues[0],
        )
        assertEquals(
            SchemaWithClazzWithEnumClazzProperty.SeasonEnumeration.SPRING,
            enumClazzPropertySchema.clazzPropertyClazz.clazz.enumValues[1],
        )
        assertEquals(
            SchemaWithClazzWithEnumClazzProperty.SeasonEnumeration.SUMMER,
            enumClazzPropertySchema.clazzPropertyClazz.clazz.enumValues[2],
        )
        assertEquals(
            SchemaWithClazzWithEnumClazzProperty.SeasonEnumeration.FALL,
            enumClazzPropertySchema.clazzPropertyClazz.clazz.enumValues[3],
        )
    }
}
