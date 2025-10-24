package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewClazzModel
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetClazzModelOfAlias
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class DocumentationExampleTest {

    // this class is the entry point
    data class EmployeePhonebook(val employees: List<Employee>)

    data class Employee(val employeeName: String, val phoneNumbers: List<PhoneNumber>)

    interface PhoneNumber {
        val phoneType: PhoneTypeEnum
        val phoneNumber: String
    }

    enum class PhoneTypeEnum {
        MOBILE,
        LANDLINE,
        FAX,
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = EmployeePhonebook::class, alias = "root")
    interface PhonebookDsl {

        @BuilderMethod
        @NewClazzModel(clazz = Employee::class, alias = "employee")
        @SetClazzModelOfAlias(alias = "root", clazzProperty = "employees", referencedAlias = "employee")
        fun employee(
            @SetAsValue(alias = "employee", clazzProperty = "employeeName") name: String,
            @InjectBuilder builder: EmployeeDsl.() -> Unit,
        ): PhonebookDsl
    }

    @Builder
    @ExpectedClazzModelFromSuperiorBuilder(clazz = Employee::class, alias = "employee")
    interface EmployeeDsl {

        @BuilderMethod
        @NewClazzModel(clazz = PhoneNumber::class, alias = "phoneEntry")
        @SetClazzModelOfAlias(alias = "employee", clazzProperty = "phoneNumbers", referencedAlias = "phoneEntry")
        fun phoneNumber(
            @SetAsValue(alias = "phoneEntry", clazzProperty = "phoneType") type: PhoneTypeEnum,
            @SetAsValue(alias = "phoneEntry", clazzProperty = "phoneNumber") phoneNumber: String,
        ): EmployeeDsl
    }

    @Test
    fun `test sourceamazing builder documentation example`() {
        val employeePhonebook: EmployeePhonebook =
            SchemaApi.withSchema(rootClazz = EmployeePhonebook::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, PhonebookDsl::class) { dsl ->
                    dsl.employee("John Smith") {
                            phoneNumber(PhoneTypeEnum.MOBILE, "+41-123-45-67")
                            phoneNumber(PhoneTypeEnum.LANDLINE, "+41-345-67-89")
                        }
                        .employee("Maggie Smith") { phoneNumber(PhoneTypeEnum.FAX, "+1-258-987-65-43") }
                }
            }

        println("Maggie's phone number: ${employeePhonebook.employees.last().phoneNumbers.first().phoneNumber}")
    }

    @Test
    fun `test sourceamazing schema documentation example`() {
        val employeePhonebook: EmployeePhonebook =
            SchemaApi.withSchema(rootClazz = EmployeePhonebook::class) { schemaContext ->
                val mobileOfJohnSmith =
                    schemaContext.dataCollector
                        .newClazzModel(clazz = PhoneNumber::class)
                        .addClazzPropertyValue(clazzProperty = "phoneNumber", value = "+41-123-45-67")
                        .addClazzPropertyValue(clazzProperty = "phoneType", value = PhoneTypeEnum.MOBILE)

                val landlineOfJohnSmith =
                    schemaContext.dataCollector
                        .newClazzModel(clazz = PhoneNumber::class)
                        .addClazzPropertyValue(clazzProperty = "phoneNumber", value = "+41-345-67-89")
                        .addClazzPropertyValue(clazzProperty = "phoneType", value = PhoneTypeEnum.LANDLINE)

                val employeeJohnSmith =
                    schemaContext.dataCollector
                        .newClazzModel(clazz = Employee::class)
                        .addClazzPropertyValue(clazzProperty = "employeeName", value = "John Smith")
                        .addClazzPropertyReference(clazzProperty = "phoneNumbers", references = mobileOfJohnSmith)
                        .addClazzPropertyReference(clazzProperty = "phoneNumbers", references = landlineOfJohnSmith)

                schemaContext.dataCollector
                    .rootClazzModel()
                    .addClazzPropertyReference(clazzProperty = "employees", references = employeeJohnSmith)
            }

        println("John's name: ${employeePhonebook.employees.first().employeeName}")
    }
}
