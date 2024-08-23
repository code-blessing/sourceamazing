package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirrorFactory
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactoryApi
import org.codeblessing.sourceamazing.schema.typemirror.OtherTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.SignatureMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
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
        val classifier = type.classifier ?: return createUndefinedTypeMirrorProvider(type)
        return when (classifier) {
            is KFunction<*> -> {
                createFunctionMirrorProvider(classifier)
            }

            is KClass<*> -> {
                if(classifier.allSupertypes.any { it.jvmErasure.qualifiedName == "kotlin.Function" }) {
                    createUndefinedTypeMirrorProvider(type)
                } else {
                    createClassMirrorProvider(classifier)
                }

            }

            else -> {
                createUndefinedTypeMirrorProvider(type)
            }
        }
    }

    private fun createUndefinedTypeMirrorProvider(ktype: KType): MirrorProvider<OtherTypeMirrorInterface> {
        return JavaReflectionOtherTypeMirror(ktype)
    }

    private fun createClassMirrorProvider(clazz: KClass<*>): MirrorProvider<ClassMirrorInterface> {
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