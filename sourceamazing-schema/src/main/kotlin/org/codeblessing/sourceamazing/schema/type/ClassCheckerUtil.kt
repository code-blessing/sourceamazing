package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.schema.exceptions.CanNotBeAnnotationTypeSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.MissingClassAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.NotInterfaceSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongAnnotationSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassModifierSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.exceptions.WrongTypeSyntaxException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

object ClassCheckerUtil {

    fun checkIsNotAnnotation(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.isAnnotation) {
            throw CanNotBeAnnotationTypeSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_BE_ANNOTATION, classDescription)
        }
    }

    fun checkIsNotPrivate(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.isPrivate) {
            throw WrongClassModifierSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_BE_PRIVATE, classDescription)
        }
    }

    fun isOrdinaryInterface(classToInspect: KClass<*>): Boolean {
        return classToInspect.isInterface && !classToInspect.isAnnotation
    }

    fun checkIsOrdinaryInterface(classToInspect: KClass<*>, classDescription: String) {
        if(!isOrdinaryInterface(classToInspect)) {
            throw NotInterfaceSyntaxException(classToInspect, SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE, classDescription)
        }
    }

    fun hasGenericTypeParameters(classToInspect: KClass<*>): Boolean {
        return classToInspect.typeParameters.isNotEmpty()
    }

    fun checkHasNoGenericTypeParameters(classToInspect: KClass<*>, classDescription: String) {
        if(hasGenericTypeParameters(classToInspect)) {
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

    fun checkHasOnlyAnnotations(permittedAnnotations: List<KClass<out Annotation>>, classToInspect: KClass<*>, classDescription: String) {
        classToInspect.annotationsIncludingSuperclasses
            .filter { it.isAnnotationFromSourceAmazing() }
            .forEach { annotationOnClass ->
                if(!permittedAnnotations.contains(annotationOnClass.annotationClass)) {
                    throw WrongAnnotationSyntaxException(classToInspect, SchemaErrorCode.CAN_NOT_HAVE_ANNOTATION, classDescription, annotationOnClass.annotationClass.annotationText())
                }
            }
    }

    fun hasMemberExtensionFunctions(classToInspect: KClass<*>): Boolean {
        return classToInspect.memberExtensionFunctions.isNotEmpty()
    }

    fun checkHasNoExtensionFunctions(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.memberExtensionFunctions.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS, classDescription, classToInspect.memberExtensionFunctions)
        }
    }

    fun checkHasNoProperties(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.memberProperties.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_HAVE_PROPERTIES, classDescription, classToInspect.memberProperties)
        }
    }

    fun hasMemberFunctions(classToInspect: KClass<*>): Boolean {
        return classToInspect.memberFunctions.filterNot { it.isFromKotlinAnyClass() }.isNotEmpty()
    }

    fun checkHasNoFunctions(classToInspect: KClass<*>, classDescription: String) {
        if(hasMemberFunctions(classToInspect)) {
            throw WrongClassStructureSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS, classDescription, classToInspect.memberFunctions)
        }
    }

    fun checkHasNoMembers(classToInspect: KClass<*>, classDescription: String) {
        if(classToInspect.members.filterNot { it is KFunction<*> && it.isFromKotlinAnyClass() }.isNotEmpty()) {
            throw WrongClassStructureSyntaxException(classToInspect, SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS_OR_PROPERTIES, classDescription, classToInspect.members)
        }
    }
}
