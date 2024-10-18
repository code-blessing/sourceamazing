package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.schema.exceptions.MissingAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberExtensionFunctions
import kotlin.reflect.full.memberProperties

object ClassCheckerUtil {

    fun checkIsOrdinaryInterface(classToInspect: KClass<*>, classDescription: String) {
        if(!classToInspect.isInterface || classToInspect.isAnnotation) {
            throw NotInterfaceSyntaxException("$classDescription '${classToInspect.longText()}' must be an interface.")
        }
    }

    fun checkHasNoGenericTypeParameters(classToInspect: KClass<*>, classDescription: String) {
        if(!classToInspect.typeParameters.isEmpty()) {
            throw WrongTypeSyntaxException("$classDescription '${classToInspect.longText()}' must not have generic type parameters but has type parameters ${classToInspect.typeParameters}.")
        }
    }

    fun checkHasAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        if(!classToInspect.hasAnnotationIncludingSuperclasses(annotation)) {
            throw MissingAnnotationSyntaxException("$classDescription '${classToInspect.longText()}' must have an annotation of type '${annotation.annotationText()}'.")
        }
    }

    fun checkHasExactNumberOfAnnotations(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String, numberOf: Int) {
        if(classToInspect.getNumberOfAnnotationIncludingSuperclasses(annotation) > numberOf) {
            throw WrongAnnotationSyntaxException("$classDescription '${classToInspect.longText()}' can not have more than $numberOf annotation of type '${annotation.annotationText()}'.")
        }
    }

    fun checkHasOnlyAnnotation(permittedAnnotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        checkHasOnlyAnnotations(listOf(permittedAnnotation), classToInspect, classDescription)
    }

    fun checkHasOnlyAnnotations(permittedAnnotations: List<KClass<out Annotation>>, classToInspect: KClass<*>, classDescription: String) {
        classToInspect.annotationsIncludingSuperclasses
            .filter { it.isAnnotationFromSourceAmazing() }
            .forEach { annotationOnClass ->
                if(!permittedAnnotations.contains(annotationOnClass.annotationClass)) {
                    throw WrongAnnotationSyntaxException("$classDescription '${classToInspect.longText()}' can not have annotation of type '${annotationOnClass.annotationClass.longText()}'.")
                }
            }
    }

    fun checkHasExactlyOneOfAnnotation(annotations: List<KClass<out Annotation>>, classToInspect: KClass<*>, classDescription: String) {
        val numberOfAnnotations = annotations.count { annotation -> classToInspect.hasAnnotationIncludingSuperclasses(annotation) }

        if(numberOfAnnotations < 1) {
            throw MissingAnnotationSyntaxException("$classDescription '${classToInspect.longText()}' must have one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        } else if(numberOfAnnotations > 1) {
            throw WrongAnnotationSyntaxException("$classDescription '${classToInspect.longText()}' can not have more than one of the annotations ${annotations.joinToString { it.annotationText() }}.")
        }
    }

    fun checkHasNoExtensionFunctions(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.declaredMemberExtensionFunctions.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, "$classDescription must not have extension functions but has ${classToInspect.declaredMemberExtensionFunctions}.")
        }
    }

    fun checkHasNoProperties(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.memberProperties.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, "$classDescription must not have member properties but has ${classToInspect.memberProperties}.")
        }
    }

    fun checkHasNoMembers(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.members.filterNot { it is KFunction<*> && it.isFromKotlinAnyClass() }.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, "$classDescription must not have any member functions or properties but has ${classToInspect.members}.")
        }
    }
}