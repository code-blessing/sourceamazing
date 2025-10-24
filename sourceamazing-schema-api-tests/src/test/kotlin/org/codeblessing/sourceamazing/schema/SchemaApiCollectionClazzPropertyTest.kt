package org.codeblessing.sourceamazing.schema

import java.util.*
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.SchemaSyntaxException
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Suppress("UNUSED")
class SchemaApiCollectionClazzPropertyTest {
    interface ReferenceClazz

    interface CommonClazz

    interface SpecificClazzOne : CommonClazz

    interface SpecificClazzTwo : CommonClazz

    enum class MyValidEnum {
        X,
        Y,
        Z,
    }

    private interface ClazzPropertyReturningCorrectCollectionTypeSchemaInterface {

        val myProperty: Set<String>
    }

    class ClazzPropertyReturningCorrectCollectionTypeSchemaClass(val myProperty: Set<String>)

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyReturningCorrectCollectionTypeSchemaInterface::class,
                ClazzPropertyReturningCorrectCollectionTypeSchemaClass::class,
            ]
    )
    fun `test adding a single value to a collection clazzProperty should not fail`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            schemaContext.dataCollector
                .rootClazzModel()
                .addClazzPropertyValue("myProperty", "text")
                .addClazzPropertyValue("myProperty", "second text")
        }
    }

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyReturningCorrectCollectionTypeSchemaInterface::class,
                ClazzPropertyReturningCorrectCollectionTypeSchemaClass::class,
            ]
    )
    fun `test adding a list of values with addClazzPropertyValue to a collection clazzProperty should throw an exception`(
        inputClass: Class<*>
    ) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<DataValidationException>(
            DataCollectionErrorCode.VALIDATION_FAILURES,
            DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", listOf("text"))
            }
        }
    }

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyReturningCorrectCollectionTypeSchemaInterface::class,
                ClazzPropertyReturningCorrectCollectionTypeSchemaClass::class,
            ]
    )
    fun `test adding a list of values with addClazzPropertyValues to a collection clazzProperty should not fail`(
        inputClass: Class<*>
    ) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            schemaContext.dataCollector.rootClazzModel().addClazzPropertyValues("myProperty", listOf("text"))
        }
    }

    private interface ClazzPropertyReturningWrongCollectionTypeSchemaInterface {

        val myProperty: SortedSet<String>
    }

    class ClazzPropertyReturningWrongCollectionTypeSchemaClass(val myProperty: SortedSet<String>)

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyReturningWrongCollectionTypeSchemaInterface::class,
                ClazzPropertyReturningWrongCollectionTypeSchemaClass::class,
            ]
    )
    fun `test a collection clazzProperty with unsupported collection type Set should throw an exception`(
        inputClass: Class<*>
    ) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_IS_WRONG_COLLECTION_CLASS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyValue("myProperty", "text")
                    .addClazzPropertyValue("myProperty", "second text")
            }
        }
    }

    private interface ClazzPropertyReturningNullableListValueSchemaInterface {

        val myProperty: List<String?>
    }

    class ClazzPropertyReturningNullableListValueSchemaClass(val myProperty: List<String?>)

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyReturningNullableListValueSchemaInterface::class,
                ClazzPropertyReturningNullableListValueSchemaClass::class,
            ]
    )
    fun `test a collection clazzProperty with nullable values should throw an exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", "text")
            }
        }
    }

    private interface ClazzPropertyReturningNullableListSchemaInterface {

        val myProperty: List<String>?
    }

    class ClazzPropertyReturningNullableListSchemaClass(val myProperty: List<String>?)

    @Disabled("inspect why this does not throw an exception")
    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyReturningNullableListSchemaInterface::class,
                ClazzPropertyReturningNullableListSchemaClass::class,
            ]
    )
    fun `test a collection clazzProperty with nullable collection should throw an exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_NULLABLE_COLLECTION_NOT_ALLOWED,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", "text")
            }
        }
    }
}
