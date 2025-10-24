package org.codeblessing.sourceamazing.schema.validation

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.datacollection.TypeSafeClazzModelImpl
import org.codeblessing.sourceamazing.schema.datacollection.validation.ClazzModelValidator
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class ClazzDataValidatorTest {

    enum class MyEnumeration {
        X,
        Y,
        Z,
    }

    enum class OtherEnumeration {
        X,
        Y,
        Z,
    }

    enum class IncompatibleEnumeration {
        A,
        B,
        C,
    }

    interface OtherClazz {
        val otherClazzTextClazzProperty: String?
    }

    interface OtherThanTheOtherClazz

    private interface SchemaForEmptyClazzValidation {
        interface EmptyClazz

        val theOnlyClazz: EmptyClazz
    }

    @Test
    fun `validate an empty list does return without exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForEmptyClazzValidation::class)
        ClazzModelValidator.validateEntries(schemaAccess, emptyList())
    }

    @Test
    fun `validate a unknown clazz throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForEmptyClazzValidation::class)
        val clazzData =
            createEmptyClazzData(OtherClazz::class) // here we define OtherClazz which is not known in this schema
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.UNKNOWN_CLAZZ,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate a duplicate clazz throws an exception`() {
        val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForEmptyClazzValidation::class)
        val clazzDataOriginal = createEmptyClazzData(SchemaForEmptyClazzValidation.EmptyClazz::class)
        val clazzDataDuplicate = createEmptyClazzData(SchemaForEmptyClazzValidation.EmptyClazz::class)
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.DUPLICATE_CLAZZ_IDENTIFIER,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzDataOriginal, clazzDataDuplicate))
        }
    }

    private interface SchemaForOneMandatoryTextClazzPropertyValidation {
        val myMandatoryText: String
    }

    private val mandatoryTextClassProperty =
        ClassProperty.of(SchemaForOneMandatoryTextClazzPropertyValidation::myMandatoryText.name)
    private val optionalTextClassProperty = ClassProperty.of("myOptionalText")

    @Test
    fun `validate unknown clazzProperty throws an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        clazzData.addValue(mandatoryTextClassProperty, "my text")
        clazzData.addValue(optionalTextClassProperty, "my text") // here we add values for an unknown clazzProperty
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.UNKNOWN_CLAZZ_PROPERTY,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate a valid entry does return without exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        clazzData.addValue(mandatoryTextClassProperty, "my text")
        ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
    }

    @Test
    fun `validate a entry with wrong type does throw an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        clazzData.addValue(mandatoryTextClassProperty, 42) // here we add a number instead of text
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate missing mandatory text clazzProperty throws an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        // here we do not add the mandatory text clazzProperty
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.MINIMUM_CARDINALITY_ERROR,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate too much values on clazzProperty throws an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForOneMandatoryTextClazzPropertyValidation::class)
        clazzData.addValue(mandatoryTextClassProperty, "my text")
        clazzData.addValue(mandatoryTextClassProperty, "my text number two")
        clazzData.addValue(mandatoryTextClassProperty, "my text number two")
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.MAXIMUM_CARDINALITY_ERROR,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    private interface SchemaForSomeEnumsClazzPropertyValidation {
        val someEnumsClazzPropertyClass: Set<MyEnumeration>
    }

    private val someEnumsClassProperty =
        ClassProperty.of(SchemaForSomeEnumsClazzPropertyValidation::someEnumsClazzPropertyClass.name)

    @Test
    fun `validate that a correct enum does return without exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForSomeEnumsClazzPropertyValidation::class)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.X)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.Y)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.X)
        ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
    }

    @Test
    fun `validate that a correct enum value passed as String does throw an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForSomeEnumsClazzPropertyValidation::class)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.X)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.Y.toString())
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.X.toString())
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate that a other enum type having the same enum structure does throw an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForSomeEnumsClazzPropertyValidation::class)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.X)
        clazzData.addValue(someEnumsClassProperty, OtherEnumeration.Y)
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate that a incompatible enum type throws an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForSomeEnumsClazzPropertyValidation::class)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.X)
        clazzData.addValue(someEnumsClassProperty, IncompatibleEnumeration.B)
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate that a wrong enum type as string throws an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForSomeEnumsClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForSomeEnumsClazzPropertyValidation::class)
        clazzData.addValue(someEnumsClassProperty, MyEnumeration.X)
        clazzData.addValue(someEnumsClassProperty, "x") // lowercase is wrong
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    private interface SchemaForReferenceClazzPropertyValidation {

        val mandatoryReferenceToOtherClazz: OtherClazz
    }

    private val mandatoryRefToOneClazzClassProperty =
        ClassProperty.of(SchemaForReferenceClazzPropertyValidation::mandatoryReferenceToOtherClazz.name)

    @Test
    fun `validate that a wrong type for reference throws an exception`() {
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceClazzPropertyValidation::class)
        val clazzData = createEmptyClazzData(SchemaForReferenceClazzPropertyValidation::class)
        clazzData.addValue(mandatoryRefToOneClazzClassProperty, "Bar")
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzData))
        }
    }

    @Test
    fun `validate that a reference pointing to a missing clazz throws an exception`() {
        val clazzModelId = ClazzModelId.of("Bar")
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceClazzPropertyValidation::class)
        val clazzDataReferencing = createEmptyClazzData(SchemaForReferenceClazzPropertyValidation::class)
        clazzDataReferencing.addValue(mandatoryRefToOneClazzClassProperty, clazzModelId)

        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzDataReferencing))
        }
    }

    @Test
    fun `validate that a reference pointing to an available clazz does return without exception`() {
        val clazzModelId = ClazzModelId.of("Bar")
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceClazzPropertyValidation::class)
        val clazzDataReferencing = createEmptyClazzData(SchemaForReferenceClazzPropertyValidation::class)
        clazzDataReferencing.addValue(mandatoryRefToOneClazzClassProperty, clazzModelId)
        val clazzDataReferenced = createEmptyClazzData(OtherClazz::class, clazzModelId)
        ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzDataReferencing, clazzDataReferenced))
    }

    @Test
    fun `validate that a reference pointing to an available clazz with wrong type throws an exception`() {
        val clazzModelId = ClazzModelId.of("Bar")
        val schemaAccess =
            SchemaCreator.createSchemaFromSchemaDefinitionClass(SchemaForReferenceClazzPropertyValidation::class)
        val clazzDataReferencing = createEmptyClazzData(SchemaForReferenceClazzPropertyValidation::class)
        clazzDataReferencing.addValue(mandatoryRefToOneClazzClassProperty, clazzModelId)
        val clazzDataReferenced = createEmptyClazzData(OtherThanTheOtherClazz::class, clazzModelId) // wrong clazz type
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.UNKNOWN_CLAZZ,
            DataCollectionErrorCode.MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
        ) {
            ClazzModelValidator.validateEntries(schemaAccess, listOf(clazzDataReferencing, clazzDataReferenced))
        }
    }

    private fun createEmptyClazzData(
        clazzClass: KClass<*>,
        clazzModelId: ClazzModelId = ClazzModelId.of("Foo"),
    ): TypeSafeClazzModel {
        return TypeSafeClazzModelImpl(1, Clazz.of(clazzClass), clazzModelId)
    }
}
