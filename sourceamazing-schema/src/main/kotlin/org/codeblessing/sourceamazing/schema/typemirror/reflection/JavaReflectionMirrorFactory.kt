package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.MethodMirror
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactoryApi
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.typemirror.ClassQualifierMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.ClassMirrorProvider
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction

object JavaReflectionMirrorFactory: MirrorFactoryApi {

    override fun convertToMirrorHierarchy(clazz: KClass<*>): ClassMirror {
        return createClassMirrorProvider(clazz).provideClassMirror()
    }

    override fun convertToMirrorHierarchy(method: Method): MethodMirror {
        val kotlinFunction = requireNotNull(method.kotlinFunction) {
            "Method $method can not be converted to a kotlin function"
        }
        return createMethodMirror(kotlinFunction)
    }

    private class ClassQualifierMirrorProvider(
        private val clazz: KClass<*>,
    ): ClassMirrorProvider {
        override fun provideClassMirror(): ClassMirror {
            return ClassMirror(
                classQualifier = createClassQualifier(clazz),
                isInterface = clazz.java.isInterface,
                isAnnotation = clazz.java.isAnnotation,
                isEnum = clazz.java.isEnum,
                annotations = createAnnotationList(clazz.annotations),
                methods = createMethodList(clazz),
                propertiesNames = clazz.memberProperties.map { it.name }
            )
        }
    }

    private fun createClassMirrorProvider(clazz: KClass<*>): ClassMirrorProvider {
        return ClassQualifierMirrorProvider(clazz)
    }

    private fun createClassMirror(clazz: KClass<*>): ClassMirror {
        return ClassMirror(
            classQualifier = createClassQualifier(clazz),
            isInterface = clazz.java.isInterface,
            isAnnotation = clazz.java.isAnnotation,
            isEnum = clazz.java.isEnum,
            annotations = createAnnotationList(clazz.annotations),
            methods = createMethodList(clazz),
            propertiesNames = clazz.memberProperties.map { it.name }
        )
    }

    private fun createClassQualifier(clazz: KClass<*>): ClassQualifierMirror {
        return ClassQualifierMirror(clazz.simpleName ?: clazz.jvmName, clazz.qualifiedName ?: "")
    }

    private fun createAnnotationList(annotations: List<Annotation>): List<AnnotationMirror> {
        return annotations
            .mapNotNull { annotation: Annotation ->
                when(annotation) {
                    is Schema -> createSchemaAnnotationMirror(annotation)
                    is Concept -> createConceptAnnotationMirror(annotation)
                    else -> null
                }
            }
    }

    private fun createMethodList(clazz: KClass<*>): List<MethodMirror> {
        return clazz.memberFunctions.map { memberFunction -> createMethodMirror(memberFunction) }
    }

    private fun createMethodMirror(memberFunction: KFunction<*>): MethodMirror {
        return MethodMirror(
            methodName = memberFunction.name,
            annotations = createAnnotationList(memberFunction.annotations),
            parameters = memberFunction.parameters.map { parameter -> createParameterMirror(parameter) },
            returnType = createTypeMirror(memberFunction.returnType),
        )
    }

    private fun createParameterMirror(parameter: KParameter): ParameterMirror {
        // TODO what is with createAnnotationList(parameter.annotations)
        return ParameterMirror(
            name = parameter.name,
            type = createTypeMirror(parameter.type),
        )
    }


    private fun createTypeMirror(type: KType): TypeMirror {
        return TypeMirror(
            annotations = createAnnotationList(type.annotations),
            classMirror = createClassMirrorProvider(type.jvmErasure), // TODO correct?
            nullable = type.isMarkedNullable

        )
    }

    private fun createSchemaAnnotationMirror(schemaAnnotation: Schema): SchemaAnnotationMirror {
        return SchemaAnnotationMirror(schemaAnnotation.concepts.map { createClassMirrorProvider(it) })
    }

    private fun createConceptAnnotationMirror(conceptAnnotation: Concept): ConceptAnnotationMirror {
        return ConceptAnnotationMirror(conceptAnnotation.facets.map { createClassMirrorProvider(it) })
    }
}