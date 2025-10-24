package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import org.junit.jupiter.api.Test

class SchemaApiConceptInterfaceTest {

    private interface EmptySchema

    @Test
    fun `test create an empty schema from an empty schema interface should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = EmptySchema::class) {
            // do nothing
        }
    }

    private sealed interface SealedInterfaceInsteadOfInterfaceSchema

    @Test
    fun `test sealed interface instead of interface schema class should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SealedInterfaceInsteadOfInterfaceSchema::class) {
            // do nothing
        }
    }

    private interface ParentInterface
    private interface SchemaWithParentInterface: ParentInterface

    @Test
    fun `test schema with parent interface without annotations should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithParentInterface::class) {
            // do nothing
        }
    }

    private abstract class AbstractClassInsteadOfInterfaceSchema

    @Test
    fun `test abstract class instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = AbstractClassInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    private class ClassInsteadOfInterfaceSchema

    @Test
    fun `test class instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = ClassInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    private enum class EnumInsteadOfInterfaceSchema

    @Test
    fun `test enum class instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = EnumInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    private object ObjectInsteadOfInterfaceSchema

    @Test
    fun `test object instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = ObjectInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    private annotation class AnnotationInsteadOfInterfaceSchema

    @Test
    fun `test annotation instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(NotInterfaceSyntaxException::class, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE) {
            SchemaApi.withSchema(schemaDefinitionClass = AnnotationInsteadOfInterfaceSchema::class) {
                // do nothing
            }
        }
    }

    @Suppress("Unused")
    private interface SchemaWithGenericTypeParameter<T>

    @Test
    fun `test schema class with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode(WrongTypeSyntaxException::class, SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithGenericTypeParameter::class) {
                // do nothing
            }
        }
    }
}
