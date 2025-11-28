package org.codeblessing.sourceamazing.utils.type

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

object KClassUtil {

    fun isAnnotationClas(classToInspect: KClass<*>): Boolean {
        return classToInspect.isAnnotation
    }

    fun isPrivateClass(classToInspect: KClass<*>): Boolean {
        return classToInspect.isPrivate
    }

    fun isOrdinaryInterface(classToInspect: KClass<*>): Boolean {
        return classToInspect.isInterface && !classToInspect.isAnnotation
    }

    fun hasGenericTypeParameters(classToInspect: KClass<*>): Boolean {
        return classToInspect.typeParameters.isNotEmpty()
    }

    fun hasAnnotation(annotation: KClass<out Annotation>, classToInspect: KClass<*>): Boolean {
        return classToInspect.hasAnnotationIncludingSuperclasses(annotation)
    }

    fun hasExactNumberOfAnnotations(
        annotation: KClass<out Annotation>,
        classToInspect: KClass<*>,
        numberOf: Int,
    ): Boolean {
        return classToInspect.getNumberOfAnnotationIncludingSuperclasses(annotation) == numberOf
    }

    fun hasOnlyAnnotations(permittedAnnotations: List<KClass<out Annotation>>, classToInspect: KClass<*>): Boolean {
        return classToInspect.annotationsIncludingSuperclasses
            .filter { it.isAnnotationFromSourceAmazing() }
            .all { annotationOnClass -> permittedAnnotations.contains(annotationOnClass.annotationClass) }
    }

    fun hasNotAnnotation(deniedAnnotation: KClass<out Annotation>, classToInspect: KClass<*>): Boolean {
        return classToInspect.annotationsIncludingSuperclasses
            .filter { it.isAnnotationFromSourceAmazing() }
            .none { annotationOnClass -> deniedAnnotation == annotationOnClass.annotationClass }
    }

    fun hasMemberExtensionFunctions(classToInspect: KClass<*>): Boolean {
        return classToInspect.memberExtensionFunctions.isNotEmpty()
    }

    fun hasMemberExtensionProperties(classToInspect: KClass<*>): Boolean {
        return classToInspect.memberExtensionFunctions.isNotEmpty()
    }

    fun hasExtensionFunctions(classToInspect: KClass<*>): Boolean {
        return classToInspect.memberExtensionFunctions.isNotEmpty()
    }

    fun hasProperties(classToInspect: KClass<*>): Boolean {
        return classToInspect.memberProperties.isNotEmpty()
    }

    fun hasMemberFunctions(classToInspect: KClass<*>): Boolean {
        return classToInspect.memberFunctions.filterNot { it.isFromKotlinAnyClass() }.isNotEmpty()
    }

    fun hasMembers(classToInspect: KClass<*>): Boolean {
        return classToInspect.members.filterNot { it is KFunction<*> && it.isFromKotlinAnyClass() }.isNotEmpty()
    }

    fun findAllCommonBaseClasses(classes: List<KClass<*>>): Set<KClass<*>> {
        val compatibleBaseClasses: MutableSet<KClass<*>> = mutableSetOf()
        val classesSet = classes.toSet()
        classes.forEach { clazz ->
            val otherClasses = classesSet - clazz
            val thisClassAndItsSuperclasses = (clazz.superclasses + clazz).toSet()
            thisClassAndItsSuperclasses.forEach { classInHierarchy ->
                val isCommonBaseClass = otherClasses.all { otherConcept -> otherConcept.isSubclassOf(classInHierarchy) }
                if (isCommonBaseClass) {
                    compatibleBaseClasses.add(classInHierarchy)
                }
            }
        }

        return compatibleBaseClasses
    }
}
