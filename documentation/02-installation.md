# Installation

Let us start and build step by step our little html form generator.
The example is also downloadable [here](./html-form-example).

This tutorial assumes that you are familiar with [Gradle](https://gradle.org/) to run 
a kotlin application in gradle using the [gradle _application_ plugin](https://docs.gradle.org/current/userguide/application_plugin.html).

## Structure of a new Gradle project

Define in your existing gradle build or in a new gradle build a new 
separate (sub-)project directory.

Finally, we will end up with the minimal structure of the following files.

```
.
├── input-data
│   ├── input-data.xml (File)
│   └── schema
├── src
│   └── main
│       ├── kotlin
│       │    └── org
│       │        └── codeblessing
│       │            └── sourceamazing
│       │                └── example
│       │                    │── HtmlFormDomainSchema.kt (File)
│       │                    └── HtmlFormDomainUnit.kt (File)
│       └── resources
│           └── META-INF
│               └── services
│                   └── org.codeblessing.sourceamazing.api.process.DomainUnit (File)
├── WebContent
├── .gitignore (File)
└── build.gradle.kts (File)
```
About the files: 
* The file _org.codeblessing.sourceamazing.api.process.DomainUnit_ will connect
the code generation of your project with the SourceAmazing framework.
* The file _input-data.xml_ contains the handwritten definitions.
* All kotlin files defined in the following sections will be but into the
_src/main/kotlin_ directory. In the example, all files are in the package
_org.codeblessing.sourceamazing.example_.
* The file _HtmlFormDomainUnit.kt_ is the entry point of the source generation.
* The _.gitignore_ file should exclude the directory _input-data/schema_.
* The _build.gradle.kts_ defines the gradle dependencies to SourceAmazing.


We will go through each of the five files and describe its content.

### _build.gradle.kts_: Define the gradle subproject


Create a new project or subproject with this Gradle script _build.gradle.kts_:

```{data-filename="build.gradle.kts"}
plugins {
    kotlin("jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codeblessing.sourceamazing:sourceamazing-api:1.0.0")
    runtimeOnly("org.codeblessing.sourceamazing:sourceamazing-engine:1.0.0")
    runtimeOnly("org.codeblessing.sourceamazing:sourceamazing-xml-schema:1.0.0")
}

application {
    mainClass.set("org.codeblessing.sourceamazing.engine.SourceamazingApplicationKt")
}

```
Some explanations:
* The `kotlin` plugin is to compile our .kt files.
* The `application` plugin let us run SourceAmazing using the command ```./gradlew run```.
* Using the ``mavenCentral`` is necessary to download the SourceAmazing dependencies.
* The main class let you run the SourceAmazing engine. This engine will search for all
  implementations of the ```org.codeblessing.sourceamazing.api.process.DomainUnit```
  using the _java service provider_ mechanism.

#### Run the code generator for the first time

Run the SourceAmazing code generator using the gradle command line:
```
./gradlew run
```

As you have not configured your project, nothing will be printed out, but SourceAmazing 
should already have been called and the gradle run command should have finished successfully.

### _HtmlFormDomainUnit.kt_: The entry point for the code generation

To have the code generation engine been kicked on with your configuration, we need to define 
an entry point for the source code generation engine.

Create a Domain Unit Class like this one:

```
package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import org.codeblessing.sourceamazing.api.process.DefaultDomainUnit
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector

class HtmlFormDomainUnit: DefaultDomainUnit<HtmlFormDomainSchema>(
    schemaDefinitionClass = HtmlFormDomainSchema::class.java
) {
    
    override fun collectTargetFiles(
        parameterAccess: ParameterAccess,
        schemaInstance: HtmlFormDomainSchema,
        targetFilesCollector: TargetFilesCollector
    ) {
        println("Successfully started HtmlFormDomainUnit!")
        // do nothing for the moment, but later fill in here the generation stuff...
    }
}

```

The file is referencing an interface `HtmlFormDomainSchema.kt`. Define this 
interface in the same package:
```
package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema

@Schema
interface HtmlFormDomainSchema {
   // currently empty
}

```

### Declare _HtmlFormDomainUnit.kt_ as a service provider

Create a new text file in the directory _META-INF/service_.
This text file is must be named _org.codeblessing.sourceamazing.api.process.DomainUnit_.
(The filename must be a fully qualified name of a java/kotlin interface and the file
content is a list of concrete implementations of this interface (a so called 
_java service provider_).

The content of this file in our example is simply:
```
org.codeblessing.sourceamazing.example.HtmlFormDomainUnit
```

What is a java service provider? 
> A Service Provider is configured and identified through a provider configuration file which is put in the resource directory META-INF/services. The file name is the fully-qualified name of the SPI (Service Provider Interface) and its content is the fully-qualified name of the SPI implementation.
>
><cite>from [www.baeldung.com/java-spi](https://www.baeldung.com/java-spi#2-service-provider-interface)</cite>

### _input-data.xml_: The source with all definitions

To run SourceAmazing without of errors, we need to create an (almost empty)
XML file:

```
<?xml version="1.0" encoding="utf-8" ?>
<sourceamazing xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema ./schema/sourceamazing-xml-schema.xsd">
    <definitions />
</sourceamazing>

```

Later we will write our definitions of the HTML form (pages, sections, input fields) into this file.

Each time you run the SourceAmazing code generator, it will create the 
file `./schema/sourceamazing-xml-schema.xsd` that is referenced in the 
header of `input-data.xml`.

### _.gitignore_: Exclude generated XSD schema file and generated HTML files

As the schema file `./schema/sourceamazing-xml-schema.xsd` is generated and you don't want to have 
generated files in your repository, you should add a _.gitignore_ file:

```
input-data/schema/*
WebContent/*

```
Additionally, we ignore all generated HTML files in the `WebContent` directory.

### Run the code generator time

Run the SourceAmazing code generator using the gradle command line:
```
./gradlew run
```

As a result, the text "Successfully started HtmlFormDomainUnit!" should 
be printed out.

Congratulation, your code generator is running.

### Next steps

It is time to define the _concepts_ and _facets_ of our HTML form code 
generator.

To learn about that, go to [Defining the input](03-defining-the-input.md).
 
