package org.codeblessing.sourceamazing.schema.schemacreator.query.proxy

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.conceptgraph.ConceptGraph
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.shortText
import org.codeblessing.sourceamazing.schema.proxy.KotlinInvocationHandler
import org.codeblessing.sourceamazing.schema.proxy.ProxyCreator
import org.codeblessing.sourceamazing.schema.schemacreator.query.QueryMethodUtil
import org.codeblessing.sourceamazing.schema.toConceptName
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

class SchemaInstanceInvocationHandler(private val conceptGraph: ConceptGraph): KotlinInvocationHandler()  {

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        function.findAnnotation<QueryConcepts>()?.let {
            val conceptClasses = it.conceptClasses
            val conceptNamesToQuery = conceptClasses.map(KClass<*>::toConceptName).toSet()
            val conceptNodes = conceptGraph.conceptsByConceptNames(conceptNamesToQuery)
            val proxyList = conceptNodes.map { conceptNode ->
                ProxyCreator.createProxy(
                    definitionClass = conceptNode.conceptName.clazz,
                    invocationHandler = ConceptInstanceInvocationHandler(conceptNode)
                ) }

            return@invoke QueryMethodUtil.adaptResultToFunctionReturnType(function, proxyList)
        }

        throw IllegalArgumentException("Method $function has not the supported annotations (${QueryConcepts::class.shortText()}.")
    }
}
