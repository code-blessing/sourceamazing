package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactoryApi
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

object JavaReflectionMirrorFactory: MirrorFactoryApi {

    override fun convertToMirrorHierarchy(clazz: KClass<*>): ClassMirrorInterface {
        return createClassMirrorProvider(clazz).provideMirror()
    }

    override fun convertToMirrorHierarchy(method: Method): FunctionMirrorInterface {
        val kotlinFunction = requireNotNull(method.kotlinFunction) {
            "Method $method can not be converted to a kotlin function"
        }
        return createFunctionMirrorProvider(kotlinFunction).provideMirror()
    }


    fun createClassMirrorProvider(clazz: KClass<*>): MirrorProvider<ClassMirrorInterface> {
        return ClassQualifierMirrorProvider(clazz)
    }

    private fun createFunctionMirrorProvider(function: KFunction<*>): MirrorProvider<FunctionMirrorInterface> {
        return FunctionMirrorProvider(function)
    }


    private data class ClassQualifierMirrorProvider(
        private val clazz: KClass<*>,
    ): MirrorProvider<ClassMirrorInterface> {
        override fun provideMirror(): JavaReflectionClassMirror {
            return JavaReflectionClassMirror(clazz)
        }
    }

    private data class FunctionMirrorProvider(
        private val function: KFunction<*>,
    ): MirrorProvider<FunctionMirrorInterface> {
        override fun provideMirror(): JavaReflectionFunctionMirror {
            return JavaReflectionFunctionMirror(function)
        }
    }
}