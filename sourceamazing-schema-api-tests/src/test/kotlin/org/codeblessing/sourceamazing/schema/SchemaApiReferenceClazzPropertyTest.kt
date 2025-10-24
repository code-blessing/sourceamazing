package org.codeblessing.sourceamazing.schema

import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Suppress("UNUSED")
class SchemaApiReferenceClazzPropertyTest {
    interface CommonClazz

    interface SpecificClazzOne : CommonClazz

    interface SpecificClazzTwo : CommonClazz

    private interface ClazzPropertyWithSelfReferenceSchemaInterface {
        val myProperty: List<ClazzPropertyWithSelfReferenceSchemaInterface>
    }

    class ClazzPropertyWithSelfReferenceSchemaClass(val myProperty: List<ClazzPropertyWithSelfReferenceSchemaClass>)

    @ParameterizedTest
    @ValueSource(
        classes =
            [ClazzPropertyWithSelfReferenceSchemaInterface::class, ClazzPropertyWithSelfReferenceSchemaClass::class]
    )
    fun `test reference clazzProperty to the same class should execute without exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            val referencedInstance = schemaContext.dataCollector.newClazzModel(schemaClass)

            schemaContext.dataCollector.rootClazzModel().addClazzPropertyReference("myProperty", referencedInstance)
        }
    }

    interface ValidClazzPropertiesSchemaInterface {

        val mySingleClazzReferenceClazzPropertyAsListOfCommonClazzInterface: List<SpecificClazzOne>

        val mySingleClazzReferenceClazzPropertyAsSetOfCommonClazzInterface: Set<SpecificClazzOne>

        val mySingleClazzReferenceClazzPropertyAsCommonClazzInterface: SpecificClazzOne

        val mySingleClazzReferenceClazzPropertyAsNullableCommonClazzInterface: SpecificClazzOne?

        val myMultipleClazzReferenceClazzPropertyAsListOfCommonClazz: List<CommonClazz>

        val myMultipleClazzReferenceClazzPropertyAsSetOfCommonClazz: Set<CommonClazz>

        val myMultipleClazzReferenceClazzPropertyAsCommonClazz: CommonClazz

        val myMultipleClazzReferenceClazzPropertyAsNullableCommonClazz: CommonClazz?
    }

    class ValidClazzPropertiesSchemaClass(
        val mySingleClazzReferenceClazzPropertyAsListOfCommonClazzInterface: List<SpecificClazzOne>,
        val mySingleClazzReferenceClazzPropertyAsSetOfCommonClazzInterface: Set<SpecificClazzOne>,
        val mySingleClazzReferenceClazzPropertyAsCommonClazzInterface: SpecificClazzOne,
        val mySingleClazzReferenceClazzPropertyAsNullableCommonClazzInterface: SpecificClazzOne?,
        val myMultipleClazzReferenceClazzPropertyAsListOfCommonClazz: List<CommonClazz>,
        val myMultipleClazzReferenceClazzPropertyAsSetOfCommonClazz: Set<CommonClazz>,
        val myMultipleClazzReferenceClazzPropertyAsCommonClazz: CommonClazz,
        val myMultipleClazzReferenceClazzPropertyAsNullableCommonClazz: CommonClazz?,
    )

    @ParameterizedTest
    @ValueSource(classes = [ValidClazzPropertiesSchemaInterface::class, ValidClazzPropertiesSchemaClass::class])
    fun `test valid clazzProperty types should return without exception`(inputClass: Class<*>) {
        val schemaClass = inputClass.kotlin
        SchemaApi.withSchema(rootClazz = schemaClass) { schemaContext ->
            val rootClazzData = schemaContext.dataCollector.rootClazzModel()
            val specificOneRef = schemaContext.dataCollector.newClazzModel(SpecificClazzOne::class)
            val specificTwoRef = schemaContext.dataCollector.newClazzModel(SpecificClazzOne::class)

            rootClazzData
                .addClazzPropertyReference("mySingleClazzReferenceClazzPropertyAsCommonClazzInterface", specificOneRef)
                .addClazzPropertyReference("myMultipleClazzReferenceClazzPropertyAsCommonClazz", specificTwoRef)
        }
    }
}
