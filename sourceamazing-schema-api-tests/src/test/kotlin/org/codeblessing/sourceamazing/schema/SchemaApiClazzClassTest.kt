package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModel
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.SchemaSyntaxException
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class SchemaApiClazzClassTest {

    private interface EmptySchemaInterface

    @Test
    fun `test create an empty schema from an empty schema interface should not fail`() {
        val schemaClass = EmptySchemaInterface::class
        SchemaApi.withSchema(rootClazz = schemaClass) {
            // nothing to do
        }
    }

    private sealed interface SealedInterfaceInsteadOfInterfaceSchema

    @Test
    fun `test sealed interface instead of interface schema class should not fail`() {
        val schemaClass = SealedInterfaceInsteadOfInterfaceSchema::class
        SchemaApi.withSchema(rootClazz = schemaClass) {
            // nothing to do
        }
    }

    private interface ParentInterface

    private interface SchemaWithParentInterface : ParentInterface

    @Test
    fun `test schema with parent interface and child interface should not fail`() {
        val schemaClass = SchemaWithParentInterface::class
        SchemaApi.withSchema(rootClazz = schemaClass) {
            // nothing to do
        }
    }

    class EmptyClassSchema

    @Test
    fun `test class instead of interface schema class should not fail`() {
        val schemaClass = EmptyClassSchema::class
        SchemaApi.withSchema(rootClazz = schemaClass) {
            // nothing to do
        }
    }

    abstract class AbstractClassSchema

    @Test
    fun `test abstract schema class class should throw an exception`() {
        val schemaClass = AbstractClassSchema::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE_OR_AN_INSTANTIABLE_CLASS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    enum class EnumClassSchema

    @Test
    fun `test enum schema class class should throw an exception`() {
        val schemaClass = EnumClassSchema::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    object ObjectInsteadOfInterfaceSchema

    @Test
    fun `test object instead of interface schema class should throw an exception`() {
        val schemaClass = ObjectInsteadOfInterfaceSchema::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.CLASS_MUST_HAVE_A_PRIMARY_CONSTRUCTOR,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private annotation class AnnotationInsteadOfInterfaceSchema

    @Test
    fun `test annotation instead of interface schema class should throw an exception`() {
        val schemaClass = AnnotationInsteadOfInterfaceSchema::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE_OR_AN_INSTANTIABLE_CLASS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private class PrivateClassSchema

    @Test
    fun `test private schema class should throw an exception`() {
        val schemaClass = PrivateClassSchema::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.CLASS_CANNOT_BE_PRIVATE,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    class PrivateConstructorClassSchema private constructor(val myClazzProperty: String)

    @Test
    fun `test public schema class with private constructor should throw an exception`() {
        val schemaClass = PrivateConstructorClassSchema::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.CLASS_PRIMARY_CONSTRUCTOR_CAN_NOT_BE_PRIVATE,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private interface SchemaWithGenericTypeParameter<T>

    @Test
    fun `test schema class with generic type parameter should throw an exception`() {
        val schemaClass = SchemaWithGenericTypeParameter::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(SchemaErrorCode.CLASS_CANNOT_HAVE_GENERIC_TYPE_PARAMETER) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    @Test
    fun `test internal ClazzModelData schema class should throw an exception`() {
        val schemaClass = ClazzModel::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(SchemaErrorCode.CLAZZ_CAN_NOT_BE_INTERNAL_CLASS) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }
}
