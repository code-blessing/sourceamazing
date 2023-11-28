package org.codeblessing.sourceamazing.engine.process.schema.proxy

import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptGraph
import org.codeblessing.sourceamazing.engine.process.schema.SchemaAnnotationConst
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import java.lang.reflect.Method

class SchemaInstanceInvocationHandler(private val conceptGraph: ConceptGraph): AbstractSchemaInvocationHandler()  {

    private val requiredMethodAnnotations = SchemaAnnotationConst.supportedSchemaAnnotations

    override fun invoke(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Any {
        val method = validateArguments(proxyOrNull, methodOrNull, argsOrNull)

        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, requiredMethodAnnotations)) {
            handleCommonAnnotations(method, conceptGraph)?.let { return it }
        }

        return InvocationHandlerHelper.handleObjectMethodsOrThrow(this, method, requiredMethodAnnotations)
    }
}
