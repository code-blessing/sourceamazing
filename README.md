# SourceAmazing ![Gradle Build](https://github.com/code-blessing/sourceamazing/actions/workflows/build-gradle-project.yml/badge.svg)

## About Source Code Generation

There are thousands of code generator frameworks generating code for 
arbitrary dedicated use cases like ...
* ... database classes representing SQL tables and fields
* ... interfaces adapters for SOAP or REST
* ... mappers to convert or serialize/deserialize data objects
* ... generators for GUI component files 
* ... etc. etc., to only mention a few cases.

On the other end, if one decide to write the code generator by hand, 
there exists a lot of different template engines. 

SourceAmazing is a helping framework if you decide to write your generated
code by hand using a template engine and not using an out-of-the-box code
generation framework.

## What is SourceAmazing

SourceAmazing helps you with the core data, which has to be maintained in 
a structured way and which is read, validated, transformed and ultimately 
passed to the template engine as model data.

With SourceAmazing, you quickly define the structure of your input data 
using java/kotlin interfaces with special annotations.

SourceAmazing provides you - based on this structure -  with XML input, kotlin
DSLs and Java Builders. Validating this structure (concerning data types, 
nullability and valid data references), reading in the structure 
data and providing this data as interfaces to feed your code generation 
templates of your choice is done completely by SourceAmazing.

And all this in a fully type-safe way, as usual in a programming language like 
kotlin. Also, there is a strong separation of mutable input sources and readonly 
output interfaces used in the templates.

### A brief example to have an idea

To have an idea how this works, let this be described as a small example.

The process is always the following:

You define your domain schema as annotated kotlin/java interfaces.
The domain schema consisting of concepts (=data entities) and its 
facets (=attributes).

That might look like this:

```
@Concept("HtmlForm")
interface HtmlFormConcept {

    @Facet("FormHeadline", mandatory = false)
    fun getFormHeadline(): String?

    @ChildConcepts(HtmlInputFieldConcept::class)
    fun getFieldsOfForm(): List<HtmlInputFieldConcept>
}

@Concept("HtmlInputField")
interface HtmlInputFieldConcept {

    @Facet("FieldName")
    fun getFieldName(): String

    ...
}

```

With help of the domain schema, the framework generates for you 
an XML schema to enter your definition data in one or multiple XML files.
```
<?xml version="1.0" encoding="utf-8" ?>
<sourceamazing xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema"
               xsi:schemaLocation="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema ./schema/sourceamazing-xml-schema.xsd">
    <definitions>
        <htmlForm formHeadline="Address Form">
            <htmlInputField fieldName="Firstname" required="true" maxFieldLength="40" />
            <htmlInputField fieldName="Lastname" required="true" maxFieldLength="50" />
        </htmlForm>
        <htmlForm>
            ...
        </htmlForm>
    </definitions>
</sourceamazing>

```

The definition data written in the XML file(s) is read and provided 
dynamically as implementations of the kotlin/java interfaces. You can 
pass this data model to your preferred template engine 
and generate any kind of files with.

## Setup, Documentation and Examples

If you are new to SourceAmazing, start with the step-by-step 
documentation at [documentation/00-step-by-step-example.md](documentation/00-step-by-step-example.md).

For a complete list of features, go to [documentation/features.md](documentation/features.md).

A glossary with the most important definition can be found at [documentation/glossary.md](documentation/glossary.md).

## License

The source code is licensed under the MIT license, which you can find in
the [MIT-LICENSE.txt](MIT-LICENSE.txt) file.
