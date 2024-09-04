package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassKind
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ClassQualifierMirror
import org.codeblessing.sourceamazing.schema.typemirror.FieldMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeParameterMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties


data class JavaReflectionClassMirror(
    private val clazz: KClass<*>,
): AbstractMirror(), ClassMirrorInterface {

    override val classQualifier: ClassQualifierMirror = createClassQualifier(clazz)
    override val classKind: ClassKind = toClassKind(clazz)
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(clazz.annotations)
    override val methods: List<FunctionMirrorInterface> = clazz.memberFunctions.map(::JavaReflectionMethodMirror)
    override val fields: List<FieldMirrorInterface> = clazz.memberProperties.map(::JavaReflectionFieldMirror)
    override val typeParameters: List<TypeParameterMirrorInterface> = clazz.typeParameters.map(::JavaReflectionTypeParameterMirror)
    override val superClasses: List<MirrorProvider<ClassMirrorInterface>> = emptyList()
    override val enumValues: List<String> = clazz.java.enumConstants?.map { it.toString() } ?: emptyList()

    override fun convertToKClass(): KClass<*> = clazz

    private fun toClassKind(clazz: KClass<*>): ClassKind {
        return if(clazz.java.isAnnotation) {
            ClassKind.ANNOTATION
        } else if(clazz.java.isInterface) {
            ClassKind.INTERFACE
        } else if(clazz.java.isEnum) {
            ClassKind.ENUM_CLASS
        } else {
            ClassKind.REGULAR_CLASS
        }
    }

    private fun createClassQualifier(clazz: KClass<*>): ClassQualifierMirror {
        // see https://youtrack.jetbrains.com/issue/KT-18104
        return ClassQualifierMirror(
            className = clazz.simpleName ?: "",
            packageName = clazz.qualifiedName?.split(".")?.dropLast(1)?.joinToString(".") ?: "",
        )
    }
}