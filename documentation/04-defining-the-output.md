# Defining the output templates and writing files

## Using the kotlin concept interfaces to fill the templates

Create a kotlin file like the one below. 
The method receives the previously defined kotlin interface 
`HTMLPageConcept` and writes an HTML page as a String.

```
package org.codeblessing.sourceamazing.example

object HtmlPageTemplate {

    fun createHtmlPageTemplate(htmlPageConcept: HtmlPageConcept): String {
        val sb =  StringBuilder()
        sb.append("<!DOCTYPE html>\n")
        sb.append("<html>\n")
        sb.append("<head><title>${htmlPageConcept.getHtmlPageTitle()}</title></head>\n")
        sb.append("<body>\n")
        sb.append("<form>\n")
        htmlPageConcept.getSectionsOfPage().forEach { htmlSection ->
            sb.append("<section>\n")
            htmlSection.getSectionName()?.let {sectionTitle ->
                sb.append("<h2>${sectionTitle}</h2>\n")
            }
            htmlSection.getFieldsInSection().forEach { htmlInputField ->
                sb.append("<label for='${htmlInputField.getFieldName()}'>${htmlInputField.getFieldName()}</label>\n")
                sb.append("<input id='${htmlInputField.getFieldName()}' type='text' ${if (htmlInputField.isInputRequired()) "required" else ""} maxlength='${htmlInputField.getMaxFieldLength()}' />\n")
            }
            sb.append("</section>\n")
        }
        sb.append("</form>\n")
        sb.append("</body>\n")
        sb.append("</html>\n")
        return sb.toString()
    }

}

```
As you can see in the example above, you can access the data in a type-safe 
way using our interfaces.

## Write the HTML files


The last thing we have to do is to connect our Template class with the 
Domain unit class.
Replace the method `collectTargetFiles` in the kotlin class 
`HtmlFormDomainUnit` with the following code:

```
import java.nio.file.Path
import java.nio.file.Paths


    override fun collectTargetFiles(
        parameterAccess: ParameterAccess,
        schemaInstance: HtmlFormDomainSchema,
        targetFilesCollector: TargetFilesCollector
    ) {
        val basePath = Paths.get("WebContent")
        schemaInstance
            .getPageConcepts()
            .forEach { page ->
                val targetFile: Path = basePath.resolve("${page.getHtmlPageTitle()}.html")
                val content: String = HtmlPageTemplate.createHtmlPageTemplate(page)
                targetFilesCollector.addFile(targetFile, content)
            }
    }

```
The code should be easy to read. We fetch all `HtmlPageConcept` instances and 
define with help of it the HTML file path and the content of the HTML file.
The SourceAmazing framework will write this file for you if you add it 
to the `targetFilesCollector`.

### Run the code generator for the third time

Run the SourceAmazing code generator again using the gradle command line:
```
./gradlew run
```

As a result, you should have in the directory _WebContent_ two HTML files 
_Company.html_ and _Person.html_ generated.

Congratulation! Your code generation journey with more sophisticated beautiful 
code has started right now.

### Next steps

The next step will be to use an alternative to XML files, a kotlin DSL.

Go for that to the guide [Using Input Builder and DSLs](05-using-input-builder-and-dsl.md).
