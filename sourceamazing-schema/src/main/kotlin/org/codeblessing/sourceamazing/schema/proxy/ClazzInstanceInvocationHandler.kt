package org.codeblessing.sourceamazing.schema.proxy

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import org.codeblessing.sourceamazing.schema.clazzgraph.ClazzInstance
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazzProperty
import org.codeblessing.sourceamazing.schema.utils.proxy.KotlinInvocationHandler

class ClazzInstanceInvocationHandler(private val clazzInstance: ClazzInstance) :
    KotlinInvocationHandler(allowMemberProperties = true, allowMemberFunctions = false) {

    override fun invoke(proxy: Any, function: KFunction<*>, arguments: List<Any?>): Any? {
        if (function is KProperty.Getter<*>) {
            val property = function.property

            val clazzPropertyName = property.name.toClazzProperty()
            return ClazzPropertyTypeAdapter.adaptClazzPropertyValue(
                clazzPropertyName,
                clazzInstance,
                property.returnType,
            )
        }

        throw IllegalArgumentException("Method $function is not a supported property.")
    }
}
