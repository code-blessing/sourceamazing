# Defining the input schema and input data

## Defining the Domain Schema with Kotlin interfaces

Let us define the main concepts as kotlin interfaces with help 
of some annotations provided by SourceAmazing.

First we define the root of all concepts, the domain schema. 
This root is annotated with a `@Schema` annotation.

```
package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
@Schema
interface HtmlFormDomainSchema {

    @ChildConcepts(HtmlPageConcept::class)
    fun getPageConcepts(): List<HtmlPageConcept>
}
```

The schema points to the first concept `HtmlPageConcept`.
That will define the HTML page concept as a kotlin interface. 
Each concept is annotated with the `@Concept` annotation. 
The facets of a concept are annotated with the `@Facet` annotation.

```
package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@Concept("HtmlPage")
interface HtmlPageConcept {

    @Facet("PageTitleName")
    fun getHtmlPageTitle(): String

    @ChildConcepts(HtmlPageSectionConcept::class)
    fun getSectionsOfPage(): List<HtmlPageSectionConcept>
}
```
Same as before, we define the section concept and its facet: 

```
package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
@Concept("HtmlPageSection")
interface HtmlPageSectionConcept {

    @Facet("SectionName", mandatory = false)
    fun getSectionName(): String?

    @ChildConcepts(HtmlInputFieldConcept::class)
    fun getFieldsInSection(): List<HtmlInputFieldConcept>
}
```

To declare a facets as not mandatory (=nullable), just overwrite the 
`mandatory` boolean flag on the `@Facet` annotation that is per default `true`.
And make the return type nullable using the question mark, here `String?`

As the last concept, we define the concept for an HTML input field and 
its facets:

```
package org.codeblessing.sourceamazing.example

import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@Concept("HtmlInputField")
interface HtmlInputFieldConcept {

    @Facet("FieldName")
    fun getFieldName(): String

    @Facet("Required")
    fun isInputRequired(): Boolean

    @Facet("MaxFieldLength")
    fun getMaxFieldLength(): Long

}
```
Note: 
The schema is only build by the values in the annotations and the 
return type of the annotated methods.
That said, it doesn't matter how you name your interfaces and your methods!

## Generate the XML schema file from the defined schema in kotlin

Run the SourceAmazing code generator using the gradle command line:
```
./gradlew run
```

It will generate or overwrite the file `./schema/sourceamazing-xml-schema.xsd`
and reflect as XML Schema your schema/possibilities in the XML file.

### Add some html form data

The _input-data.xml_ file defines the content of our HTML forms.

It's time to add some HTML forms inside the `<definintions>` XML tag:
```
<?xml version="1.0" encoding="utf-8" ?>
<sourceamazing xmlns="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="https://codeblessing.org/sourceamazing/sourceamazing-xml-schema ./schema/sourceamazing-xml-schema.xsd">
    <definitions>
        <htmlPage pageTitleName="Person">
            <htmlPageSection sectionName="Names">
                <htmlInputField fieldName="Firstname" required="true" maxFieldLength="40" />
                <htmlInputField fieldName="Lastname" required="true" maxFieldLength="50" />
            </htmlPageSection>
            <htmlPageSection sectionName="Address">
                <htmlInputField fieldName="Street" required="false" maxFieldLength="60" />
                <htmlInputField fieldName="Zip" required="true" maxFieldLength="15" />
                <htmlInputField fieldName="Town" required="true" maxFieldLength="40" />
                <htmlInputField fieldName="Country" required="true" maxFieldLength="40" />
            </htmlPageSection>
        </htmlPage>
        <htmlPage pageTitleName="Company">
            <htmlPageSection sectionName="Company Information">
                <htmlInputField fieldName="Company Name" required="true" maxFieldLength="200" />
                <htmlInputField fieldName="Company Main Country" required="true" maxFieldLength="40" />
            </htmlPageSection>
        </htmlPage>
    </definitions>
</sourceamazing>

```
If you edit this file in an XML editor, the editor will assist you in 
creating the XML tags and XML attributes.
This is a result of referencing the XML schema and you will have content 
assistance and validation comfort when writing additional XML content.

Run the SourceAmazing code generator again using the gradle command line:
```
./gradlew run
```
If you have validation errors, read carefully the error messages SourceAmazing 
is printing out. They should be detailed and self-explaining.

### Next steps

As we have the input schema and concrete input data, we
should write some templates to generate HTML pages.

To learn about that, go to [Defining the output](04-defining-the-output.md).
 
