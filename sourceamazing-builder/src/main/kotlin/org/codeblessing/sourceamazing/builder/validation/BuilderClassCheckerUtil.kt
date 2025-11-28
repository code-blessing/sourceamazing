package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.utils.type.*
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isAnnotationClass
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isOrdinaryInterface
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isPrivateClass

object BuilderClassCheckerUtil {

    fun checkIsNotAnnotation(classToInspect: KClass<*>, classDescription: String) {
        if (isAnnotationClass(classToInspect)) {
            throw BuilderSyntaxException(classToInspect, BuilderErrorCode.CLASS_CANNOT_BE_ANNOTATION, classDescription)
        }
    }

    fun checkIsNotPrivate(classToInspect: KClass<*>, classDescription: String) {
        if (isPrivateClass(classToInspect)) {
            throw BuilderSyntaxException(classToInspect, BuilderErrorCode.CLASS_CANNOT_BE_PRIVATE, classDescription)
        }
    }

    fun checkIsOrdinaryInterface(classToInspect: KClass<*>, classDescription: String) {
        if (!isOrdinaryInterface(classToInspect)) {
            throw BuilderSyntaxException(classToInspect, BuilderErrorCode.CLASS_MUST_BE_AN_INTERFACE, classDescription)
        }
    }


    fun checkHasNoGenericTypeParameters(classToInspect: KClass<*>, classDescription: String) {
        if (hasGenericTypeParameters(classToInspect)) {
            throw BuilderSyntaxException(
                classToInspect,
                BuilderErrorCode.NO_GENERIC_TYPE_PARAMETER,
                classDescription,
                classToInspect.typeParameters,
            )
        }
    }

    fun checkHasAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>, classDescription: String) {
        if (!classToInspect.hasAnnotationIncludingSuperclasses(annotation)) {
            throw BuilderSyntaxException(
                classToInspect,
                BuilderErrorCode.MUST_HAVE_ANNOTATION,
                classDescription,
                annotation.annotationText(),
            )
        }
    }

    fun checkHasExactNumberOfAnnotations(
        annotation: KClass<out Annotation>,
        classToInspect: KClass<*>,
        classDescription: String,
        numberOf: Int,
    ) {
        if (classToInspect.getNumberOfAnnotationIncludingSuperclasses(annotation) != numberOf) {
            throw BuilderSyntaxException(
                classToInspect,
                BuilderErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS,
                classDescription,
                numberOf,
                annotation.annotationText(),
            )
        }
    }

    fun checkHasOnlyAnnotations(
        permittedAnnotations: List<KClass<out Annotation>>,
        classToInspect: KClass<*>,
        classDescription: String,
    ) {
        classToInspect.annotationsIncludingSuperclasses
            .filter { it.isAnnotationFromSourceAmazing() }
            .forEach { annotationOnClass ->
                if (!permittedAnnotations.contains(annotationOnClass.annotationClass)) {
                    throw BuilderSyntaxException(
                        classToInspect,
                        BuilderErrorCode.CAN_NOT_HAVE_ANNOTATION,
                        classDescription,
                        annotationOnClass.annotationClass.annotationText(),
                    )
                }
            }
    }

    fun checkHasNoExtensionFunctions(classToInspect: KClass<*>, classDescription: String) {
        if (classToInspect.memberExtensionFunctions.isNotEmpty()) {
            throw BuilderSyntaxException(
                classToInspect,
                BuilderErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS,
                classDescription,
                classToInspect.memberExtensionFunctions,
            )
        }
    }

    fun checkHasNoProperties(classToInspect: KClass<*>, classDescription: String) {
        if (classToInspect.memberProperties.isNotEmpty()) {
            throw BuilderSyntaxException(
                classToInspect,
                BuilderErrorCode.CLASS_CANNOT_HAVE_PROPERTIES,
                classDescription,
                classToInspect.memberProperties,
            )
        }
    }
}
