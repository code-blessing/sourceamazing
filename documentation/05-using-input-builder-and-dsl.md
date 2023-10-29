# Creating Builders and DSLs

You learned how to get data from XML files with a generated XSD schema.

Another approach is to write the data directly in kotlin as DSL or using the builder pattern.



## Preparations to create the builder interfaces

Create an empty interface to define your the root interface for the input.

```
package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*

@DataCollector
interface HtmlFormInputSchema {
  // more is here to come
}
```

Then define a new domain unit class like this:

```
class HtmlFormDslDomainUnit: DomainUnit<HtmlFormDomainSchema, HtmlFormInputSchema>(
    schemaDefinitionClass = HtmlFormDomainSchema::class.java,
    inputDefinitionClass = HtmlFormInputSchema::class.java,
) {

    override fun collectInputData(
        parameterAccess: ParameterAccess,
        extensionAccess: DataCollectionExtensionAccess,
        dataCollector: HtmlFormInputSchema
    ) { 
        // here, we will define our DSL definitions later
    }


    override fun collectTargetFiles(
        parameterAccess: ParameterAccess,
        schemaInstance: HtmlFormDomainSchema,
        targetFilesCollector: TargetFilesCollector
    ) {
        val basePath = Paths.get("WebContent")
        schemaInstance
            .getPageConcepts()
            .forEach { page ->
                val targetFile = basePath.resolve("${page.getHtmlPageTitle()}.html")
                val content = HtmlPageTemplate.createHtmlPageTemplate(page)
                targetFilesCollector.addFile(targetFile, content)
            }
    }
}
```

This class looks in most parts the same as the already created `HtmlFormDomainUnit`.

One difference is, that we extend directly from the `DomainUnit` interface, instead of 
the provided default implementation `org.codeblessing.sourceamazing.api.process.DefaultDomainUnit`.
With that comes the need to implement/override the method `collectInputData` to collect 
the input data. 

The method `collectTargetFiles` to write the target files remains exactly the same.

Let us have a deeper look at the class definition:

```
class HtmlFormDslDomainUnit: DomainUnit<HtmlFormDomainSchema, HtmlFormInputSchema>(
    schemaDefinitionClass = HtmlFormDomainSchema::class.java,
    inputDefinitionClass = HtmlFormInputSchema::class.java,
)
```
We not only define the domain schema how the data can be used (getter methods) but also 
a separate interface `HtmlFormInputSchema` how data can be added (setter methods).

Now we will write those setter methods.

## Writing builder interfaces

Add the first builder to build HtmlPage concept:

```
package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*

@DataCollector
interface HtmlPageBuilder {

}
```

Then extend the `HtmlFormInputSchema` with the following method:

```
...

    @AddConceptAndFacets(HtmlPageBuilder::class)
    @ConceptNameValue("HtmlPage")
    @AutoRandomConceptIdentifier
    fun addHtmlPage(@FacetValue("PageTitleName") pageTitle: String, @ConceptBuilder builder: HtmlPageBuilder.() -> Unit)

...

```

Here a description about all the annotations:
* The `@AddConceptAndFacets(HtmlPageBuilder::class)` annotation let the framework know, that this method 
creates a new concept and additionally some of its facets. The method should return (for builders) or 
inject (for DSLs) an instance of `HtmlPageBuilder`.
* The `@ConceptNameValue("HtmlPage")` annotation let the framework know, what concept is created.
* Each concept must have a unique identifier. This is to reference other concepts. If you do not want 
your instance of the HTMLPageConcept to be referenced, you can tell the framework to create a random 
concept identifier, using the `@AutoRandomConceptIdentifier` annotation.
> **_Defining ConceptIdentifier_**
>
> Each concept must have a unique identifier. This is to reference other concepts.
>
> The `@AutoRandomConceptIdentifier` annotation generates for you a random concept identifier.
> If you want to set the concept identifier manually, this can be done
>
> ```
> fun addHtmlPage(@ConceptIdentifier htmlPageConceptId: ConceptIdentifier, ...)
> ```
> or in its short form
> ```
> fun addHtmlPage(@ConceptIdentifier htmlPageConceptId: String, ...)
> ```
>

* The `@FacetValue("PageTitleName") pageTitle: String` part in the method forces the user of your 
`addHtmlPage` method to define the pageTitle directly when creating the HtmlPage concept.
* The `@ConceptBuilder builder: HtmlPageBuilder.() -> Unit` part will let the framework inject an 
implementation of the HtmlPageBuilder. To have a nice DSL feeling, this Builder is injected as the 
`this` parameter.

> **_Using the builder pattern_**  
> If you prefer the Builder pattern, you can leave away the last parameter
> `@ConceptBuilder builder: HtmlPageBuilder.() -> Unit` and instead return the `HtmlPageBuilder`.
>
> That would then look like this:
> ```
>     // ... same interface, same annotations ase above
>     fun addHtmlPage(@FacetValue("PageTitleName") pageTitle: String): HtmlPageBuilder
>
> ```

Now that we have the root input interface, we want to continue with the _HtmlPage_ builder:

```
package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*

@DataCollector
interface HtmlPageBuilder {

    @ConceptNameValue("HtmlPageSection")
    @AddConceptAndFacets(HtmlPageSectionBuilder::class)
    @AutoRandomConceptIdentifier
    fun addHtmlSection(@ConceptBuilder builder: HtmlPageSectionBuilder.() -> Unit)

    @ConceptNameValue("HtmlPageSection")
    @AddConceptAndFacets(HtmlPageSectionBuilder::class)
    @AutoRandomConceptIdentifier
    fun addHtmlSection(@FacetValue("SectionName") sectionName: String, @ConceptBuilder builder: HtmlPageSectionBuilder.() -> Unit)

}
```

Here, we again define methods to create a _HtmlPageSection_ concept instance.
The sole difference between the two methods is, that the second method allows (and forces) to define
a section name. Both return the `HtmlPageSectionBuilder` to add more data to the _HtmlPageSection_ 
concept instance. 

Here it is:
```
package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet

@DataCollector
interface HtmlPageSectionBuilder {

    @AddFacets
    fun setSectionName(@FacetValue("SectionName") sectionName: String?)

    @AddConceptAndFacets(HtmlInputFieldBuilder::class)
    @ConceptNameValue("HtmlInputField")
    @AutoRandomConceptIdentifier
    fun addInputField(@FacetValue("FieldName") fieldName: String,
                      @FacetValue("Required") required: Boolean = true,
                      @FacetValue("MaxFieldLength") maxFieldLength: Long = 255)

    @AddConceptAndFacets(HtmlInputFieldBuilder::class)
    @ConceptNameValue("HtmlInputField")
    @AutoRandomConceptIdentifier
    fun addInputField(@FacetValue("FieldName") fieldName: String,
                      @ConceptBuilder builder: HtmlInputFieldBuilder.() -> Unit)

}
```

What is new here is a method to only set a facet and not create a new child concept instance.
```
    @AddFacets
    fun setSectionName(@FacetValue("SectionName") sectionName: String?)
```
This method adds only one or many facets.

The other two methods `addInputField` create an instance of a _HtmlInputField_. The need 
the builder `HtmlInputFieldBuilder`, which looks like this:

```
package org.codeblessing.sourceamazing.example.dsl

import org.codeblessing.sourceamazing.api.process.datacollection.annotations.AddFacets
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.DataCollector
import org.codeblessing.sourceamazing.api.process.datacollection.annotations.FacetValue

@DataCollector
interface HtmlInputFieldBuilder {

    @AddFacets
    fun setRequired(@FacetValue("Required") required: Boolean)

    @AddFacets
    fun setMaxFieldLength(@FacetValue("MaxFieldLength") maxFieldLength: Long)

}
```
As the _HtmlInputField_ has no child concepts, you can only set facets on this builder.
With this last builder, we are complete and can start using this DSLs/Builders.

# Using the builder interfaces

As defined at the beginning, we have on the `HtmlFormDslDomainUnit` class the new method 
`collectInputData` that have to be overwritten. One parameter of this method is our first 
input interface `HtmlFormInputSchema`.

We can now use this interface to define a new HTML page, in this example to register a DNS domain.

```
    override fun collectInputData(
        parameterAccess: ParameterAccess,
        extensionAccess: DataCollectionExtensionAccess,
        dataCollector: HtmlFormInputSchema
    ) {
        dataCollector.addHtmlPage("Domains") {
            addHtmlSection {
                setSectionName("Domain ")
                addInputField("DNS Domain Name", required = true, maxFieldLength = 255)
            }
            addHtmlSection("Domain Holder Information") {
                addInputField("firstname", required = true, maxFieldLength = 255)
                addInputField("lastname") {
                    setRequired(true)
                    setMaxFieldLength(128)
                }
            }
        }
    }

```
Instead of using the XML files, we used here our new DSL to get data into the data model.

### Next steps

There are some more features SourceAmazing provides.

To learn about that, go to the [Features](features.md) page.

