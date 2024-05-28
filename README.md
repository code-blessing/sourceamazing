# SourceAmazing ![Gradle Build](https://github.com/code-blessing/sourceamazing/actions/workflows/build-gradle-project.yml/badge.svg)

## What is SourceAmazing

SourceAmazing is a small framework to read data from sources like XML, 
Java/Kotlin Builders or Java/Kotlin DSLs into Java/Kotlin classes.
You write a schema with help of Java/Kotlin interfaces with some annotations 
and SourceAmazing provides all the necessary corresponding implementations 
to get your data into this schema, including 
* XSD-schemas, if you want to read from XML files
* implementations of Builders and/or DSLs, if you want to read from Java/Kotlin code
* the validation of the data concerning data types, nullability and data references

### A brief example to have an idea

To have an idea how this works, let's make a small example.
We will read some phone book data of employees of a company.

The process is always the following:

#### Define your Data Schema

You define your data schema as annotated kotlin/java interfaces.
The tools you have to define this data schema are: 
* _facets_: attributes/properties of a certain type, like String, Boolean, Integer, Reference to other concepts, Enum
* _concepts_: data entities, which group some facets. 
* _schema_: Root entry point which bundle all concepts together.

That might look like this:

```kotlin
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.Concept

import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue

//
// Schema (entry point)
//

@Schema(concepts = [
    Employee::class,
    PhoneNumber::class,
])
interface EmployeePhonebook {
  
    @QueryConcepts(conceptClasses = [Employee::class])
    fun getAllEmployees(): List<Employee>
}

//
// Employee
//

@Concept(facets = [
    Employee.EmployeeName::class,
    Employee.PhoneNumbersOfEmployee::class,
])
interface Employee {

    @StringFacet(minimumOccurences=0)
    interface EmployeeName

    @ReferenceFacet(minimumOccurrences=0, maximumOccurrences=10, referencedConcepts=[PhoneNumber::class])
    interface PhoneNumbersOfEmployee

    @QueryFacetValue(EmployeeName::class)
    fun getEmployeeName(): String?

    @QueryFacetValue(PhoneNumbersOfEmployee::class)
    fun getPhoneNumbers(): List<PhoneNumber>
}

//
// PhoneNumber of an Employee
//

@Concept(facets = [
    PhoneNumber.PhoneType::class,
    PhoneNumber.PhoneNumber::class,
])
interface PhoneNumber {
    
    @EnumFacet(enumerationClass = PhoneTypeEnum::class)
    interface PhoneType

    @StringFacet
    interface PhoneNumber

    @QueryFacetValue(PhoneType::class)
    fun getPhoneType(): PhoneTypeEnum

    @QueryFacetValue(PhoneNumber::class)
    fun getPhoneNumber(): String
}

enum class PhoneTypeEnum {
    MOBILE,
    LANDLINE,
    FAX,
}

```

To get data into your application, you have to use a short snippet of code to read and
validate this data.

We will use here a file path to an XML file to import the data from the file `phonebook-data.xml`.

```kotlin
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.xmlschema.api.XmlSchemaApi
import java.nio.file.Paths
import java.nio.file.Path

fun readPhonebookData() {
    val pathToXmlFile: Path = Paths.get("phonebook-data.xml")

    val phonebook: EmployeePhonebook = SchemaApi.withSchema(EmployeePhonebook::class) { schemaContext: SchemaContext ->
        XmlSchemaApi.createXsdSchemaAndReadXmlFile(schemaContext, pathToXmlFile)
    }

    println("My Employees: ${phonebook.getAllEmployees()}")
}

```

#### Write data in an XML file

With help of the schema interfaces, the framework generates for you an XML schema 
(at `./schema/sourceamazing-xml-schema.xsd`) that helps you to enter 
your data in one (or many) XML files.

Here an example XML file with some written phonebook data:
```
<?xml version="1.0" encoding="utf-8" ?>
<!-- File: phonebook-data.xml -->
<sourceamazing xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema"
               xsi:schemaLocation="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema ./schema/sourceamazing-xml-schema.xsd">
    <definitions>
        <employee employeeName="John Doe">
            <phoneNumbersOfEmployee>
                <phoneNumber phoneType="MOBILE" phoneNumber="+1 (555) 555-1234" />
                <phoneNumber phoneType="LANDLINE" phoneNumber="+1 (555) 789-6543" />
            </phoneNumbersOfEmployee>
        </employee>
        <employee>
            ...
        </employee>
    </definitions>
</sourceamazing>

```
The employees written in the XML file(s) is read and provided 
dynamically as implementations of the kotlin/java interfaces. You can 
access this data model in your Java/Kotlin application.

### Write data as Kotlin Builders or DSLs

Maybe you prefer to write your employees and their phone numbers as 
kotlin Builders or as a Kotlin DSL instead of the XML file.

To define the builder syntax, we write some additional interface classes 
using the annotations provided to declare, what the builder methods have 
to do, when they are called.

```kotlin
@Builder
interface EmployeePhonebookBuilder {
  
    @BuilderMethod
    @NewConcept(Employee::class, "theEmployee") // when calling this method, create a new Employee concept instance
    @SetRandomConceptIdentifierValue("theEmployee") // every concept instance needs a mandatory concept identifier, set a random one
    @WithNewBuilder(PhoneNumberBuilder::class) // define what this method will return
    fun addEmployee(
        @SetFacetValue("theEmployee", Employee.EmployeeName::class) name: String
    ): PhoneNumberBuilder
}

@Builder
@ExpectedAliasFromSuperiorBuilder("theEmployee")
interface PhoneNumberBuilder {
    
    @BuilderMethod
    @NewConcept(PhoneNumber::class, "thePhoneNumber") // when calling this method, create a new PhoneNumber concept instance
    @SetRandomConceptIdentifierValue("thePhoneNumber") // every concept instance needs a mandatory concept identifier, set a random one
    @SetAliasConceptIdentifierReferenceFacetValue("theEmployee", Employee.PhoneNumbersOfEmployee::class, "thePhoneNumber") // attach the new created 'thePhoneNumber' concept to the concept 'theEmployee' (to facet 'PhoneNumbersOfEmployee') 
    @WithNewBuilder(PhoneNumberBuilder::class) // define what this method will return
    fun addPhoneNumber(
        @SetFacetValue("thePhoneNumber", PhoneNumber.PhoneType::class) type: PhoneTypeEnum,
        @SetFacetValue("thePhoneNumber", PhoneNumber.PhoneNumber::class) phoneNumber: String,
    ): PhoneNumberBuilder
}
```

Now as the builder syntax is declared, we can use those interfaces and write some phonebook entries:

```kotlin
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.SchemaContext
import org.codeblessing.sourceamazing.builder.api.BuilderApi

fun readPhonebookData() {
    val phonebook: EmployeePhonebook = SchemaApi.withSchema(EmployeePhonebook::class) { schemaContext: SchemaContext ->

        BuilderApi.withBuilder(schemaContext, EmployeePhonebookBuilder::class) { builder: EmployeePhonebookBuilder ->
            builder
                .addEmployee("John Doe")
                    .addPhoneNumber(PhoneTypeEnum.MOBILE, "+1 (555) 555-1234")
                    .addPhoneNumber(PhoneTypeEnum.LANDLINE, "+1 (555) 789-6543")

            // ...                
            //builder.addEmployee("Other employee").addPhoneNumber(...)
        }
    }

    println("My Employees: ${phonebook.getAllEmployees()}")
}

```
Of course, you can mix XML data imports and as many different Builders/DSL imports as you like. 

## Setup, Documentation and Examples

If you are new to SourceAmazing, start with the step-by-step 
documentation at [Wiki](https://github.com/code-blessing/sourceamazing/wiki).
There, you also find a complete list of features and a glossary with the most important definition.

## License

The source code is licensed under the MIT license, which you can find in
the [MIT-LICENSE.txt](MIT-LICENSE.txt) file.
