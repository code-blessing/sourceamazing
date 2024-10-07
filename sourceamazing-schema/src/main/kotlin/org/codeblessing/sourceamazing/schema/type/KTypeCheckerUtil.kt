package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.exceptions.WrongFunctionSyntaxException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

object KTypeCheckerUtil {

    fun classFromProjection(projection: KTypeProjection, functionToInspect: KFunction<*>, functionDescription: String): KClass<*> {
        if(projection.variance == null) {
            throw WrongFunctionSyntaxException("$functionDescription can not have a star-type projection for function return type. Method: $functionToInspect ")
        }
        if(projection.variance == KVariance.IN) {
            throw WrongFunctionSyntaxException("$functionDescription can not have an In-variant projection for function return type. Method: $functionToInspect ")
        }
        val type = projection.type ?: throw WrongFunctionSyntaxException("$functionDescription must have a declared function return type. Method: $functionToInspect ")

        return classFromType(type, functionToInspect, functionDescription)
    }

    fun classFromType(type: KType, functionToInspect: KFunction<*>, functionDescription: String): KClass<*> {
        val classifier = type.classifier
        if(classifier == null || classifier !is KClass<*>) {
            throw WrongFunctionSyntaxException(
                "$functionDescription must have a return type that is a class but was $classifier. Function: $functionToInspect"
            )
        }

        return classifier
    }

}