package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassQualifierMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirror
import org.codeblessing.sourceamazing.schema.typemirror.MirrorFactoryApi
import org.codeblessing.sourceamazing.schema.typemirror.ParameterMirror
import org.codeblessing.sourceamazing.schema.typemirror.ReturnMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
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
        return createClassMirrorProvider(clazz).provideMirror()
    }

    override fun convertToMirrorHierarchy(method: Method): FunctionMirror {
        val kotlinFunction = requireNotNull(method.kotlinFunction) {
            "Method $method can not be converted to a kotlin function"
        }
        return createMethodMirror(kotlinFunction)
    }

    private class ClassQualifierMirrorProvider(
        private val clazz: KClass<*>,
    ): MirrorProvider<ClassMirror> {
        override fun provideMirror(): ClassMirror {
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

    private fun createClassMirrorProvider(clazz: KClass<*>): MirrorProvider<ClassMirror> {
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

    private fun createMethodList(clazz: KClass<*>): List<FunctionMirror> {
        return clazz.memberFunctions.map { memberFunction -> createMethodMirror(memberFunction) }
    }

    private fun createMethodMirror(memberFunction: KFunction<*>): FunctionMirror {
        return FunctionMirror(
            functionName = memberFunction.name,
            annotations = createAnnotationList(memberFunction.annotations),
            parameters = memberFunction.parameters.map(this::createParameterMirror),
            returnType = createReturnMirror(memberFunction.returnType),
            receiverParameterType = memberFunction.parameters
                .filter { it.kind == KParameter.Kind.EXTENSION_RECEIVER }
                .map(this::createParameterMirror)
                .firstOrNull()
        )
    }

    private fun createParameterMirror(parameter: KParameter): ParameterMirror {
        return ParameterMirror(
            name = parameter.name,
            type = createTypeMirror(parameter.type),
            annotations = createAnnotationList(parameter.annotations),
        )
    }

    private fun createReturnMirror(type: KType): ReturnMirror {
        return ReturnMirror(
            type = createTypeMirror(type),
            annotations = createAnnotationList(type.annotations)
        )
    }


    private fun createTypeMirror(type: KType): TypeMirror {
        return TypeMirror(
            signatureMirror = createClassMirrorProvider(type.jvmErasure), // TODO correct?
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