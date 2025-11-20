package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.processtest.formschema.FormBuilder
import org.codeblessing.sourceamazing.processtest.formschema.FormData
import org.codeblessing.sourceamazing.processtest.formschema.FormSchema
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.SchemaApi
// import org.codeblessing.sourceamazing.xmlschema.api.XmlSchemaApi
import java.nio.file.Paths

private val pathToXmlFile = Paths.get("input-data").resolve("input-data.xml")

fun main() {
    val formSchema = SchemaApi.withSchema(FormSchema::class) { schemaContext ->
        val rootConceptData = schemaContext.dataCollector.newConceptData(ConceptName.of(FormSchema::class))

        // TODO activate XML schema as soon as it supports root concepts
        //  XmlSchemaApi.createXsdSchemaAndReadXmlFile(schemaContext, pathToXmlFile)
        BuilderApi.withBuilder(
            schemaContext = schemaContext,
            builderClass = FormBuilder::class,
        ) { dataCollector ->
            FormData.collectFormData(dataCollector)
        }
        rootConceptData.conceptIdentifier
    }

    formSchema.forms.forEach { form ->
        println("----------------")
        println(form.formTitle)
        println("----------------")
        println(ProcesstestTemplate.formContent(form))
    }

}
