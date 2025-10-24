package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.datacollection.ClazzModel
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.SchemaSyntaxException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Suppress("UNUSED")
class SchemaApiClazzClassStructureTest {
    enum class MyValidEnum {
        X,
        Y,
        Z,
    }

    private interface TextClazzPropertySchemaInterface {

        val myText: String?
    }

    class TextClazzPropertySchemaClass(val myText: String?)

    @ParameterizedTest
    @ValueSource(classes = [TextClazzPropertySchemaInterface::class, TextClazzPropertySchemaClass::class])
    fun `test text clazzProperty should not fail`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myText", "text")
        }
    }

    private interface ClazzPropertyWithValidEnumClazzPropertySchemaInterface {

        val myEnum: MyValidEnum?
    }

    class ClazzPropertyWithValidEnumClazzPropertySchemaClass(val myEnum: MyValidEnum?)

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyWithValidEnumClazzPropertySchemaInterface::class,
                ClazzPropertyWithValidEnumClazzPropertySchemaClass::class,
            ]
    )
    fun `test enumeration clazzProperty with a valid enumeration type should not fail`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myEnum", MyValidEnum.X)
        }
    }

    private enum class MyPrivateEnum {
        A,
        B,
        C,
    }

    private interface ClazzPropertyWithPrivateEnumClazzPropertySchemaInterface {
        val myEnum: MyPrivateEnum
    }

    @Test
    fun `test enum clazzProperty that has private modifier should throw an exception`() {
        // this test can only be setup for interfaces as public classes
        // do not allow to pass private enums as constructor parameter

        val schemaClass = ClazzPropertyWithPrivateEnumClazzPropertySchemaInterface::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(SchemaErrorCode.CLAZZ_PROPERTY_ENUM_HAS_PRIVATE_MODIFIER) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myEnum", MyPrivateEnum.A)
            }
        }
    }

    private interface ClazzPropertyHavingMembersSchemaInterface {

        val myText: String

        fun oneMemberOnClazzPropertyInterface()
    }

    @Test
    fun `test clazz having members functions on it should throw an exception`() {
        val schemaClass = ClazzPropertyHavingMembersSchemaInterface::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.INTERFACE_CANNOT_HAVE_MEMBER_FUNCTIONS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myText", "Text")
            }
        }
    }

    object ClazzPropertyObject

    private interface ClazzPropertyObjectSchemaInterface {

        val myProperty: ClazzPropertyObject
    }

    class ClazzPropertyObjectSchemaClass(val myProperty: ClazzPropertyObject)

    @ParameterizedTest
    @ValueSource(classes = [ClazzPropertyObjectSchemaInterface::class, ClazzPropertyObjectSchemaClass::class])
    fun `test clazz with object clazzProperty should not fail`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", ClazzPropertyObject)
        }
    }

    annotation class ClazzPropertyAnnotationInterfaceInsteadOfInterface

    private interface ClazzPropertyAnnotationSchemaInterface {

        val myProperty: ClazzPropertyAnnotationInterfaceInsteadOfInterface?
    }

    class ClazzPropertyAnnotationSchemaClass(val myProperty: ClazzPropertyAnnotationInterfaceInsteadOfInterface?)

    @ParameterizedTest
    @ValueSource(classes = [ClazzPropertyAnnotationSchemaInterface::class, ClazzPropertyAnnotationSchemaClass::class])
    fun `test clazzProperty annotation instead of interface should throw an exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE_OR_AN_INSTANTIABLE_CLASS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private interface ClazzModelDataContainingSchemaInterface {

        val myProperty: ClazzModel?
    }

    class ClazzModelDataContainingSchemaClass(val myProperty: ClazzModel?)

    @ParameterizedTest
    @ValueSource(classes = [ClazzModelDataContainingSchemaInterface::class, ClazzModelDataContainingSchemaClass::class])
    fun `test clazzProperty having ClazzModelData as class type should throw an exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(SchemaErrorCode.CLAZZ_CAN_NOT_BE_INTERNAL_CLASS) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private interface ClazzPropertyWithUnitTypeOnClazzPropertySchemaInterface {
        val myProperty: Unit
    }

    class ClazzPropertyWithUnitTypeOnClazzPropertySchemaClass(val myProperty: Unit)

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyWithUnitTypeOnClazzPropertySchemaInterface::class,
                ClazzPropertyWithUnitTypeOnClazzPropertySchemaClass::class,
            ]
    )
    fun `test clazzProperty with Unit type should throw an exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.CLAZZ_PROPERTY_CANNOT_BE_UNIT_TYPE,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", Unit)
            }
        }
    }

    private interface ClazzPropertyWithFunctionTypeSchemaInterface {
        val myProperty: () -> Unit
    }

    class ClazzPropertyWithFunctionTypeSchemaClass(val myProperty: () -> Unit)

    @ParameterizedTest
    @ValueSource(
        classes = [ClazzPropertyWithFunctionTypeSchemaInterface::class, ClazzPropertyWithFunctionTypeSchemaClass::class]
    )
    fun `test clazzProperty with function should throw an exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_TYPE_IS_WRONG_COLLECTION_CLASS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                val lambda = {}
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", lambda)
            }
        }
    }

    private interface ClazzPropertyWithTypeParameterSchemaInterface {
        val myProperty: Pair<String, Int>
    }

    class ClazzPropertyWithTypeParameterSchemaClass(val myProperty: Pair<String, Int>)

    @ParameterizedTest
    @ValueSource(
        classes =
            [ClazzPropertyWithTypeParameterSchemaInterface::class, ClazzPropertyWithTypeParameterSchemaClass::class]
    )
    fun `test clazzProperty with type parameter should throw an exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.TYPE_TO_CLASS_DECONSTRUCTION_IS_INVALID_ONLY_COLLECTION_OR_CLASS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                val lambda = {}
                schemaContext.dataCollector.rootClazzModel().addClazzPropertyValue("myProperty", lambda)
            }
        }
    }

    private fun interface ClazzPropertyWithFunctionalInterface {
        fun getMyStrings(): List<String>
    }

    @Test
    fun `test functional interface SAM with one method should fail`() {
        val schemaClass = ClazzPropertyWithFunctionalInterface::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.INTERFACE_CANNOT_HAVE_MEMBER_FUNCTIONS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private interface ClazzPropertyReturningGenericParameterSchemaInterface {

        fun <A> getMyClazzPropertyValuesAsListOfString(): List<A>
    }

    @Test
    fun `test clazzProperty returning collection with generic collection type parameter should throw an exception`() {
        val schemaClass = ClazzPropertyReturningGenericParameterSchemaInterface::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.INTERFACE_CANNOT_HAVE_MEMBER_FUNCTIONS,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private interface ClazzPropertyWithGetterClazzPropertySchemaInterface {
        val myProperty: List<Any>
            get() = emptyList()
    }

    @Test
    fun `test clazzProperty having a method body should throw an exception`() {
        val schemaClass = ClazzPropertyWithGetterClazzPropertySchemaInterface::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.PROPERTY_MUST_BE_ABSTRACT,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    private interface ClazzPropertyWithExtensionReceiverClazzPropertySchemaInterface {

        val Int.myProperty: String
    }

    @Test
    fun `test clazzProperty with extension receiver should throw an exception`() {
        val schemaClass = ClazzPropertyWithExtensionReceiverClazzPropertySchemaInterface::class
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE,
            SchemaErrorCode.PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE,
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) {
                // nothing to do
            }
        }
    }

    interface EmptyClazz

    @AdditionallyKnownClasses([])
    private interface ClazzPropertyWithEmptyReferenceClazzPropertySchemaInterface {
        val myProperty: List<EmptyClazz>
    }

    @AdditionallyKnownClasses([])
    class ClazzPropertyWithEmptyReferenceClazzPropertySchemaClass(val myProperty: List<EmptyClazz>)

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyWithEmptyReferenceClazzPropertySchemaInterface::class,
                ClazzPropertyWithEmptyReferenceClazzPropertySchemaClass::class,
            ]
    )
    fun `test clazz having empty list of classes in AdditionallyKnownClazzModels annotation should throw an exception`(
        inputClass: Class<*>
    ) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(SchemaErrorCode.ADDITIONAL_CLAZZS_ANNOTATION_LIST_EMPTY) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                val referencedInstance = schemaContext.dataCollector.newClazzModel(EmptyClazz::class)

                schemaContext.dataCollector.rootClazzModel().addClazzPropertyReference("myProperty", referencedInstance)
            }
        }
    }

    @AdditionallyKnownClasses([EmptyClazz::class, EmptyClazz::class])
    private interface ClazzPropertyWithDuplicateReferenceClazzPropertySchemaInterface {
        val myProperty: List<EmptyClazz>
    }

    @AdditionallyKnownClasses([EmptyClazz::class, EmptyClazz::class])
    class ClazzPropertyWithDuplicateReferenceClazzPropertySchemaClass(val myProperty: List<EmptyClazz>)

    @ParameterizedTest
    @ValueSource(
        classes =
            [
                ClazzPropertyWithDuplicateReferenceClazzPropertySchemaInterface::class,
                ClazzPropertyWithDuplicateReferenceClazzPropertySchemaClass::class,
            ]
    )
    fun `test clazz having duplicate classes in AdditionallyKnownClazzModels should throw an exception`(
        inputClass: Class<*>
    ) {
        val schemaClass = inputClass.kotlin
        assertExceptionWithErrorCode<SchemaSyntaxException>(
            SchemaErrorCode.ADDITIONAL_CLAZZS_ANNOTATION_CONTAINS_DUPLICATES
        ) {
            SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
                val referencedInstance = schemaContext.dataCollector.newClazzModel(EmptyClazz::class)

                schemaContext.dataCollector.rootClazzModel().addClazzPropertyReference("myProperty", referencedInstance)
            }
        }
    }

    interface SimpleObject {
        val simpleName: String
    }

    interface ValidClazzPropertiesSchemaInterface {

        val myTextClazzPropertyAsListOfString: List<String>

        val myTextClazzPropertyAsSetOfString: Set<String>

        val myTextClazzPropertyAsString: String

        val myTextClazzPropertyAsNullableString: String?

        val myNumberClazzPropertyAsListOfInt: List<Int>

        val myNumberClazzPropertyAsSetOfInt: Set<Int>

        val myNumberClazzPropertyAsInt: Int

        val myNumberClazzPropertyAsNullableInt: Int?

        val myBoolClazzPropertyAsListOfBoolean: List<Boolean>

        val myBoolClazzPropertyAsSetOfBoolean: Set<Boolean>

        val myBoolClazzPropertyAsBoolean: Boolean

        val myBoolClazzPropertyAsNullableBoolean: Boolean?

        val myEnumerationClazzPropertyAsListOfEnums: List<MyValidEnum>

        val myEnumerationClazzPropertyAsSetOfEnums: Set<MyValidEnum>

        val myEnumerationClazzPropertyAsEnum: MyValidEnum

        val myEnumerationClazzPropertyAsNullableEnum: MyValidEnum?

        val mySimpleObjectClazzPropertyAsListOfEnums: List<SimpleObject>
        val mySimpleObjectClazzPropertyAsSetOfEnums: Set<SimpleObject>
        val mySimpleObjectClazzPropertyAsEnum: SimpleObject
        val mySimpleObjectClazzPropertyAsNullableEnum: SimpleObject?
    }

    class ValidClazzPropertiesSchemaClass(
        val myTextClazzPropertyAsListOfString: List<String>,
        val myTextClazzPropertyAsSetOfString: Set<String>,
        val myTextClazzPropertyAsString: String,
        val myTextClazzPropertyAsNullableString: String?,
        val myNumberClazzPropertyAsListOfInt: List<Int>,
        val myNumberClazzPropertyAsSetOfInt: Set<Int>,
        val myNumberClazzPropertyAsInt: Int,
        val myNumberClazzPropertyAsNullableInt: Int?,
        val myBoolClazzPropertyAsListOfBoolean: List<Boolean>,
        val myBoolClazzPropertyAsSetOfBoolean: Set<Boolean>,
        val myBoolClazzPropertyAsBoolean: Boolean,
        val myBoolClazzPropertyAsNullableBoolean: Boolean?,
        val myEnumerationClazzPropertyAsListOfEnums: List<MyValidEnum>,
        val myEnumerationClazzPropertyAsSetOfEnums: Set<MyValidEnum>,
        val myEnumerationClazzPropertyAsEnum: MyValidEnum,
        val myEnumerationClazzPropertyAsNullableEnum: MyValidEnum?,
        val mySimpleObjectClazzPropertyAsListOfEnums: List<SimpleObject>,
        val mySimpleObjectClazzPropertyAsSetOfEnums: Set<SimpleObject>,
        val mySimpleObjectClazzPropertyAsEnum: SimpleObject,
        val mySimpleObjectClazzPropertyAsNullableEnum: SimpleObject?,
    )

    @ParameterizedTest
    @ValueSource(classes = [ValidClazzPropertiesSchemaInterface::class, ValidClazzPropertiesSchemaClass::class])
    fun `test valid clazzProperty types should return without exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            val simpleClazzModel =
                schemaContext.dataCollector
                    .newClazzModel(SimpleObject::class)
                    .addClazzPropertyValue("simpleName", "FooBarSimpleName")

            schemaContext.dataCollector
                .rootClazzModel()
                .addClazzPropertyValue("myTextClazzPropertyAsListOfString", "Foo")
                .addClazzPropertyValue("myTextClazzPropertyAsSetOfString", "Foo")
                .addClazzPropertyValue("myTextClazzPropertyAsString", "Foo")
                .addClazzPropertyValue("myNumberClazzPropertyAsListOfInt", 42)
                .addClazzPropertyValue("myNumberClazzPropertyAsSetOfInt", 42)
                .addClazzPropertyValue("myNumberClazzPropertyAsInt", 42)
                .addClazzPropertyValue("myBoolClazzPropertyAsListOfBoolean", true)
                .addClazzPropertyValue("myBoolClazzPropertyAsSetOfBoolean", false)
                .addClazzPropertyValue("myBoolClazzPropertyAsBoolean", true)
                .addClazzPropertyValue("myEnumerationClazzPropertyAsListOfEnums", MyValidEnum.X)
                .addClazzPropertyValue("myEnumerationClazzPropertyAsSetOfEnums", MyValidEnum.Y)
                .addClazzPropertyValue("myEnumerationClazzPropertyAsEnum", MyValidEnum.Z)
                .addClazzPropertyValue("myEnumerationClazzPropertyAsNullableEnum", MyValidEnum.Z)
                .addClazzPropertyReference("mySimpleObjectClazzPropertyAsListOfEnums", simpleClazzModel)
                .addClazzPropertyReference("mySimpleObjectClazzPropertyAsSetOfEnums", simpleClazzModel)
                .addClazzPropertyReference("mySimpleObjectClazzPropertyAsEnum", simpleClazzModel)
                .addClazzPropertyReference("mySimpleObjectClazzPropertyAsNullableEnum", simpleClazzModel)
        }
    }
}
