package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorFactory
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactoryApi
import org.codeblessing.sourceamazing.schema.typemirror.OtherTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.SignatureMirror
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

    fun createSignatureMirrorProvider(type: KType): MirrorProvider<out SignatureMirror> {
        val classifier = type.classifier ?: return createOtherTypeMirrorProvider(type)
        val hasTypeParameter = type.arguments.any { argument: KTypeProjection ->
            return@any argument.type?.classifier is KTypeParameter
        }
        if(hasTypeParameter) {
            return createTypeParameterTypeMirrorProvider(type)
        }
        // read https://kt.academy/article/ak-reflection-type
        return when (classifier) {
            is KFunction<*> -> {
                createFunctionMirrorProvider(classifier)
            }

            is KClass<*> -> {
                if(classifier.isSubclassOf(Function::class)) {
                    createFunctionMirrorProviderFromAnonymousFunctionType(type)
                } else {
                    createClassMirrorProvider(classifier)
                }

            }

            else -> {
                createOtherTypeMirrorProvider(type)
            }
        }
    }

    private fun createOtherTypeMirrorProvider(ktype: KType): MirrorProvider<OtherTypeMirrorInterface> {
        return JavaReflectionOtherTypeMirror(ktype)
    }

    private fun createFunctionMirrorProviderFromAnonymousFunctionType(kType: KType): MirrorProvider<FunctionMirrorInterface> {
        return GenericMirrorProvider { JavaReflectionAnonymousFunctionTypeMirror(kType) }
    }


    private fun createTypeParameterTypeMirrorProvider(kType: KType): MirrorProvider<TypeParameterTypeMirrorInterface> {
        return GenericMirrorProvider { JavaReflectionTypeParameterTypeMirror(kType) }
    }

    private fun createClassMirrorProvider(clazz: KClass<*>): MirrorProvider<ClassMirrorInterface> {
        return GenericMirrorProvider { JavaReflectionClassMirror(clazz) }
    }

    private fun createFunctionMirrorProvider(function: KFunction<*>): MirrorProvider<FunctionMirrorInterface> {
        return GenericMirrorProvider { JavaReflectionMethodMirror(function) }
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