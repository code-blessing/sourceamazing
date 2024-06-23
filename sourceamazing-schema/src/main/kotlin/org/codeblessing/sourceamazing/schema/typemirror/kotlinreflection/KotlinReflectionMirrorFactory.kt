package org.codeblessing.sourceamazing.schema.typemirror.kotlinreflection

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
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmName

object KotlinReflectionMirrorFactory: MirrorFactoryApi {

    override fun convertToMirrorHierarchy(clazz: KClass<*>): ClassMirror {
        return createClassMirror(clazz)
    }

    /**
     * Collector to avoid circular dependencies.
     *
     * TODO solve problem that Mirrors are immutable and therefore
     * appended to the map at the end.
     * Possible solution: Use builders...
     */
    private class MirrorCollector(clazz: KClass<*>) {
        val registeredTypes: MutableMap<KClass<*>, TypeMirror> = mutableMapOf()

    }

    private fun createClassMirror(clazz: KClass<*>): ClassMirror {
        return ClassMirror(
            className = clazz.simpleName ?: clazz.jvmName,
            isInterface = clazz.java.isInterface,
            isAnnotation = clazz.java.isAnnotation,
            annotations = createAnnotationList(clazz.annotations),
            methods = createMethodList(clazz),
            propertiesNames = clazz.memberProperties.map { it.name }
        )
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
        return clazz.memberFunctions
            .map { memberFunction ->
                MethodMirror(
                    methodName = memberFunction.name,
                    annotations = createAnnotationList(memberFunction.annotations),
                    parameters = memberFunction.parameters.map { parameter -> createParameterMirror(parameter) },
                    returnType = createTypeMirror(memberFunction.returnType),
                )
            }
    }

    private fun createParameterMirror(parameter: KParameter): ParameterMirror {
        return ParameterMirror(
            name = parameter.name,
            annotations = createAnnotationList(parameter.annotations),
            type = createTypeMirror(parameter.type),
        )
    }


    private fun createTypeMirror(type: KType): TypeMirror {
        return TypeMirror(
            annotations = createAnnotationList(type.annotations),

        )
    }

    private fun createSchemaAnnotationMirror(schemaAnnotation: Schema): SchemaAnnotationMirror {
        return SchemaAnnotationMirror(schemaAnnotation)
    }

    private fun createConceptAnnotationMirror(conceptAnnotation: Concept): ConceptAnnotationMirror {
        return ConceptAnnotationMirror(conceptAnnotation)
    }
}