package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.reflection.JavaReflectionMirrorFactory
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * The mirror classes are an abstraction of class, methods and annotations
 * to represent all the needed data independent of kotlin reflection or
 * java reflection.
 *
 * It is primarily introduced to make testing more easy, as you do not have
 * to provide all constellations with interfaces and real annotations but
 * can give as test data a predefined and reusable test set of mirrors.
 *
 * Whereas the mirror classes, methods and types are held very open to represent
 * all kind of the real representation, the mirror annotations in contrast only
 * do represent the real annotations provided in sourceamazing (Schema, Concept,
 * etc.). Wrong constellations are on annotation level prohibited by the compiler
 * and don't have to be tested.
 */
object MirrorFactory {
    private val factoryImplementation: MirrorFactoryApi = JavaReflectionMirrorFactory

    fun convertToClassMirror(clazz: KClass<*>): ClassMirrorInterface {
        return factoryImplementation.convertToMirrorHierarchy(clazz)
    }

    fun convertToMethodMirror(method: Method): FunctionMirrorInterface {
        return factoryImplementation.convertToMirrorHierarchy(method)
    }
}