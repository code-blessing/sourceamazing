# SourceAmazing ![Gradle Build](https://github.com/code-blessing/sourceamazing/actions/workflows/build-gradle-project.yml/badge.svg)

## What is SourceAmazing

SourceAmazing is a small Kotlin framework that lets you define easy-to-read
DSLs and builders for collecting structured data — such as configurations or
code generation input — without having to implement them yourself.

You define your DSL using annotated Kotlin interfaces, and SourceAmazing
provides all the necessary implementations dynamically.

### How it works

The process is always the following:
1. You define your data classes as Kotlin classes or interfaces.
2. You define Builder/DSL interfaces that describe how your data is collected.
3. You collect your data.

To see how this works, let's walk through a small example that reads
*phone book data of employees* from a company.

#### 1. Define your data classes

Define your data schema as Kotlin classes or interfaces.

```kotlin

// this class is the entry point
data class EmployeePhonebook(
    val employees: List<Employee>
)

data class Employee(
    val employeeName: String,
    val phoneNumbers: List<PhoneNumber>,
)

interface PhoneNumber {
    val phoneType: PhoneTypeEnum
    val phoneNumber: String
}

enum class PhoneTypeEnum {
    MOBILE,
    LANDLINE,
    FAX,
}

```

#### 2. Define the DSL/Builders for collecting your data

Define the builder/DSL as regular Kotlin interfaces. Use annotations to
specify how the method parameters map to your data classes.

```kotlin
//import org.codeblessing.sourceamazing.builder.api.annotations.Builder
//import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
//import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedClazzModelFromSuperiorBuilder
//import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
//import org.codeblessing.sourceamazing.builder.api.annotations.NewClazzModel
//import org.codeblessing.sourceamazing.builder.api.annotations.SetAsValue
//import org.codeblessing.sourceamazing.builder.api.annotations.SetClazzModelOfAlias


@Builder
@ExpectedClazzModelFromSuperiorBuilder(clazz = EmployeePhonebook::class, alias = "root")
interface PhonebookDsl {

    @BuilderMethod
    @NewClazzModel(clazz = Employee::class, alias = "employee")
    @SetClazzModelOfAlias(alias = "root", clazzProperty = "employees", referencedAlias = "employee")
    fun employee(
        @SetAsValue(alias = "employee", clazzProperty = "employeeName") 
        name: String,
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
        @SetAsValue(alias = "phoneEntry", clazzProperty = "phoneType") 
        type: PhoneTypeEnum,
        @SetAsValue(alias = "phoneEntry", clazzProperty = "phoneNumber") 
        phoneNumber: String,
    ): EmployeeDsl
}

```
#### 3. Collect your data with your Builder/DSL

Then use the previously defined DSL/builders:

```kotlin
//import org.codeblessing.sourceamazing.schema.api.SchemaApi
//import org.codeblessing.sourceamazing.builder.api.BuilderApi

    fun collectAndPrintSomePhonebookData() {
        val employeePhonebook: EmployeePhonebook =
            SchemaApi.withSchema(EmployeePhonebook::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, PhonebookDsl::class) { dsl ->
                    dsl
                        .employee("John Smith") {
                            phoneNumber(PhoneTypeEnum.MOBILE, "+41-123-45-67")
                            phoneNumber(PhoneTypeEnum.LANDLINE, "+41-345-67-89")
                        }
                        .employee("Maggie Smith") {
                            phoneNumber(PhoneTypeEnum.FAX, "+1-258-987-65-43")
                        }
                }
            }

        println("Maggie's phone number: ${employeePhonebook.employees.last().phoneNumbers.first().phoneNumber}")
    }
```

As you can see, you don't need to implement the Builder/DSL interfaces or write
any code to instantiate and wire up the data classes.

## Setup, Documentation and Examples

If you are new to SourceAmazing, start with the step-by-step documentation at [Wiki](https://github.com/code-blessing/sourceamazing/wiki).

## License

The source code is licensed under the MIT license, which you can find in
the [MIT-LICENSE.txt](MIT-LICENSE.txt) file.
