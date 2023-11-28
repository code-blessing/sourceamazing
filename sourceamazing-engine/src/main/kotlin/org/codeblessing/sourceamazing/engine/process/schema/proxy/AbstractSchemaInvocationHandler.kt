package org.codeblessing.sourceamazing.engine.process.schema.proxy

import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptNode
import org.codeblessing.sourceamazing.engine.process.conceptgraph.SortedChildrenConceptNodesProvider
import org.codeblessing.sourceamazing.engine.process.schema.SchemaAnnotationConst
import org.codeblessing.sourceamazing.engine.proxy.InvocationHandlerHelper
import org.codeblessing.sourceamazing.engine.proxy.ProxyCreator
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.KClass

abstract class AbstractSchemaInvocationHandler: InvocationHandler {

    protected fun validateArguments(proxyOrNull: Any?, methodOrNull: Method?, argsOrNull: Array<out Any>?): Method {
        InvocationHandlerHelper.requiredProxy(proxyOrNull, methodOrNull)
        val method: Method = InvocationHandlerHelper.validatedMethod(methodOrNull)
        InvocationHandlerHelper.validatedArguments(methodOrNull, argsOrNull)
        return method
    }

    protected fun handleCommonAnnotations(method: Method, conceptNodesProvider: SortedChildrenConceptNodesProvider): Any? {
        if(InvocationHandlerHelper.isMethodAnnotatedWithExactlyOneOf(method, SchemaAnnotationConst.listInstanceChildConceptAnnotations)) {
            return SchemaInvocationHandlerHelper.mapToListProxy(method, conceptNodesProvider) { interfaceClass: KClass<*>, childConceptNode: ConceptNode ->
                ProxyCreator.createProxy(interfaceClass.java, SchemaConceptInstanceInvocationHandler(childConceptNode))
            }
        }

        return null
    }
}
