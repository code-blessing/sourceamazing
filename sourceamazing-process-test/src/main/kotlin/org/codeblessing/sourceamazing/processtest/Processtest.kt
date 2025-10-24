package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.processtest.formschema.FormBuilder
import org.codeblessing.sourceamazing.processtest.formschema.FormData
import org.codeblessing.sourceamazing.processtest.formschema.FormSchema
import org.codeblessing.sourceamazing.schema.api.SchemaApi

fun main() {
    val formSchema =
        SchemaApi.withSchema<FormSchema> { schemaContext ->
            BuilderApi.withBuilder(schemaContext = schemaContext, builderClass = FormBuilder::class) { dataCollector ->
                FormData.collectFormData(dataCollector)
            }
        }

    formSchema.forms.forEach { form ->
        println("----------------")
        println(form.formTitle)
        println("----------------")
        println(ProcesstestTemplate.formContent(form))
    }
}
