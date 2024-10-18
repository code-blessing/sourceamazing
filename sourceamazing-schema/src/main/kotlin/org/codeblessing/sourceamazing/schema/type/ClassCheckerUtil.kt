package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.exceptions.MissingClassAnnotationSyntaxException
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
            throw NotInterfaceSyntaxException(classToInspect, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE, classDescription)
        }
    }

    fun checkHasNoGenericTypeParameters(classToInspect: KClass<*>, classDescription: String) {
        if(!classToInspect.typeParameters.isEmpty()) {
            throw WrongTypeSyntaxException(SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER, classDescription, classToInspect.typeParameters)
        }
    }

    fun checkHasAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        if(!classToInspect.hasAnnotationIncludingSuperclasses(annotation)) {
            throw MissingClassAnnotationSyntaxException(classToInspect, SchemaErrorCode.MUST_HAVE_ANNOTATION, classDescription, annotation.annotationText())
        }
    }

    fun checkHasExactNumberOfAnnotations(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String, numberOf: Int) {
        if(classToInspect.getNumberOfAnnotationIncludingSuperclasses(annotation) > numberOf) {
            throw WrongAnnotationSyntaxException(classToInspect, SchemaErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS, classDescription, numberOf, annotation.annotationText())
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
                    throw WrongAnnotationSyntaxException(classToInspect, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION, classDescription, annotationOnClass.annotationClass.annotationText())
                }
            }
    }

    fun checkHasExactlyOneOfAnnotation(annotations: List<KClass<out Annotation>>, classToInspect: KClass<*>, classDescription: String) {
        val numberOfAnnotations = annotations.count { annotation -> classToInspect.hasAnnotationIncludingSuperclasses(annotation) }

        if(numberOfAnnotations < 1) {
            throw MissingClassAnnotationSyntaxException(classToInspect, SchemaErrorCode.MUST_HAVE_ONE_OF_THE_FOLLOWING_ANNOTATIONS, classDescription, annotations.joinToString { it.annotationText() })
        } else if(numberOfAnnotations > 1) {
            throw WrongAnnotationSyntaxException(classToInspect, SchemaErrorCode.NOT_MULTIPLE_ANNOTATIONS, classDescription, annotations.joinToString { it.annotationText() })
        }
    }

    fun checkHasNoExtensionFunctions(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.declaredMemberExtensionFunctions.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS, classDescription, classToInspect.declaredMemberExtensionFunctions)
        }
    }

    fun checkHasNoProperties(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.memberProperties.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_HAVE_PROPERTIES, classDescription, classToInspect.memberProperties)
        }
    }

    fun checkHasNoMembers(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.members.filterNot { it is KFunction<*> && it.isFromKotlinAnyClass() }.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS_OR_PROPERTIES, classDescription, classToInspect.members)
        }
    }
}