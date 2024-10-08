package org.codeblessing.sourceamazing.schema.type

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

object KClassUtil {

    fun findAllCommonBaseClasses(classes: List<KClass<*>>): Set<KClass<*>> {
        val compatibleBaseClasses: MutableSet<KClass<*>> = mutableSetOf()
        val classesSet = classes.toSet()
        classes.forEach { clazz ->
            val otherClasses = classesSet - clazz
            val thisClassAndItsSuperclasses = (clazz.superclasses + clazz).toSet()
            thisClassAndItsSuperclasses.forEach { classInHierarchy ->
                val isCommonBaseClass = otherClasses.all { otherConcept ->  otherConcept.isSubclassOf(classInHierarchy) }
                if(isCommonBaseClass) {
                    compatibleBaseClasses.add(classInHierarchy)
                }
            }
        }

        return compatibleBaseClasses
    }
}