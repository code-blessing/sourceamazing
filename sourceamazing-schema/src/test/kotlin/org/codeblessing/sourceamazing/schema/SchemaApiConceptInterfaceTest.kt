package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongClassStructureSyntaxException
import org.junit.jupiter.api.Test

@Suppress("UNUSED", "Unused")
class SchemaApiConceptInterfaceTest {

    private interface EmptySchema

    @Test
    fun `test create an empty schema from an empty schema interface should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = EmptySchema::class) { schemaContext ->
            withRootInstance<EmptySchema>(schemaContext) {
                // do nothing
            }
        }
    }

    private sealed interface SealedInterfaceInsteadOfInterfaceSchema

    @Test
    fun `test sealed interface instead of interface schema class should not fail`() {
        SchemaApi.withSchema(
            schemaDefinitionClass = SealedInterfaceInsteadOfInterfaceSchema::class
        ) { schemaContext ->
            withRootInstance<SealedInterfaceInsteadOfInterfaceSchema>(schemaContext) {
                // do nothing
            }
        }
    }

    private interface ParentInterface

    private interface SchemaWithParentInterface : ParentInterface

    @Test
    fun `test schema with parent interface without annotations should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithParentInterface::class) {
            schemaContext ->
            withRootInstance<SchemaWithParentInterface>(schemaContext) {
                // do nothing
            }
        }
    }

    private abstract class AbstractClassInsteadOfInterfaceSchema

    @Test
    fun `test abstract class instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = AbstractClassInsteadOfInterfaceSchema::class
            ) { schemaContext ->
                withRootInstance<AbstractClassInsteadOfInterfaceSchema>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private class ClassInsteadOfInterfaceSchema

    @Test
    fun `test class instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = ClassInsteadOfInterfaceSchema::class) {
                schemaContext ->
                withRootInstance<ClassInsteadOfInterfaceSchema>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private enum class EnumInsteadOfInterfaceSchema

    @Test
    fun `test enum class instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = EnumInsteadOfInterfaceSchema::class) {
                schemaContext ->
                withRootInstance<EnumInsteadOfInterfaceSchema>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private object ObjectInsteadOfInterfaceSchema

    @Test
    fun `test object instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = ObjectInsteadOfInterfaceSchema::class) {
                schemaContext ->
                withRootInstance<ObjectInsteadOfInterfaceSchema>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private annotation class AnnotationInsteadOfInterfaceSchema

    @Test
    fun `test annotation instead of interface schema class should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE,
        ) {
            SchemaApi.withSchema(
                schemaDefinitionClass = AnnotationInsteadOfInterfaceSchema::class
            ) { schemaContext ->
                withRootInstance<AnnotationInsteadOfInterfaceSchema>(schemaContext) {
                    // do nothing
                }
            }
        }
    }

    private interface SchemaWithGenericTypeParameter<T>

    @Test
    fun `test schema class with generic type parameter should throw an exception`() {
        assertExceptionWithErrorCode(
            WrongClassStructureSyntaxException::class,
            SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER,
        ) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithGenericTypeParameter::class) {
                schemaContext ->
                withRootInstance<Any>(schemaContext) {
                    // do nothing
                }
            }
        }
    }
}
