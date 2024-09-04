package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorFactory
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ClassTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactoryApi
import org.codeblessing.sourceamazing.schema.typemirror.OtherTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeParameterTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.isSubclassOf
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

    fun createAnnotationList(annotations: List<Annotation>): List<AnnotationMirror> {
        return AnnotationMirrorFactory.createAnnotationMirrorList(annotations) {
            createClassMirrorProvider(it)
        }
    }

    fun createTypeMirrorProvider(kType: KType): TypeMirrorInterface {
        // TODO distinguish between class/function/other types here
        val classifier = kType.classifier ?: return createOtherTypeMirror(kType)
        val hasTypeParameter = kType.arguments.any { argument: KTypeProjection ->
            return@any argument.type?.classifier is KTypeParameter
        }
        if(hasTypeParameter) {
            return createTypeParameterTypeMirrorProvider(kType)
        }
        // TODO read https://kt.academy/article/ak-reflection-type
        return when (classifier) {
            is KFunction<*> -> {
                // TODO will this branch ever be selected?
                createFunctionTypeMirrorProvider(kType, classifier)
            }
            is KClass<*> -> {
                if(classifier.isSubclassOf(Function::class)) {
                    createFunctionTypeMirrorProvider(kType)
                } else {
                    createClassTypeMirrorProvider(kType, classifier)
                }
            }
            else -> {
                createOtherTypeMirror(kType)
            }
        }
    }

    private fun createOtherTypeMirror(ktype: KType): OtherTypeMirrorInterface {
        return JavaReflectionOtherTypeMirror(ktype)
    }

    private fun createFunctionMirrorProviderFromFunctionType(kType: KType): FunctionMirrorInterface {
        return JavaReflectionFunctionFromFunctionTypeMirror(kType)
    }


    private fun createTypeParameterTypeMirrorProvider(kType: KType): TypeParameterTypeMirrorInterface {
        return JavaReflectionTypeParameterTypeMirror(kType)
    }

    private fun createClassMirrorProvider(clazz: KClass<*>): MirrorProvider<ClassMirrorInterface> {
        return GenericMirrorProvider { JavaReflectionClassMirror(clazz) }
    }

    private fun createFunctionMirrorProvider(function: KFunction<*>): MirrorProvider<FunctionMirrorInterface> {
        return GenericMirrorProvider { JavaReflectionMethodMirror(function) }
    }

    private fun createFunctionTypeMirrorProvider(kType: KType): FunctionTypeMirrorInterface {
        val function = createFunctionMirrorProviderFromFunctionType(kType)
        return JavaReflectionFunctionTypeMirror(kType, function)
    }

    private fun createFunctionTypeMirrorProvider(kType: KType, kFunction: KFunction<*>): FunctionTypeMirrorInterface {
        return JavaReflectionFunctionTypeMirror(kType, createFunctionMirrorProvider(kFunction))
    }

    private fun createClassTypeMirrorProvider(kType: KType, clazz: KClass<*>): ClassTypeMirrorInterface {
        val genericTypeArguments = kType.arguments
            .mapNotNull { it.type }
            .map { createTypeMirrorProvider(it) }
        return JavaReflectionClassTypeMirror(kType, createClassMirrorProvider(clazz), genericTypeArguments)
    }

    private class GenericMirrorProvider<T>(
        private val factory: () -> T,
    ): MirrorProvider<T> {
        private var instance: T? = null
        override fun provideMirror(): T {
            if(instance == null) {
                instance = factory()
            }
            return instance!!
        }
    }
}