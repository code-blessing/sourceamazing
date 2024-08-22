package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import java.util.*
import kotlin.reflect.KClass

object AnnotationMirrorFactory {
    private val lockObject = Any()
    private var isServiceLoaderCalled = false
    private val registeredAnnotations: MutableMap<KClass<out Annotation>, AnnotationMirrorCreator> = mutableMapOf()

    fun createAnnotationMirrorList(annotations: List<Annotation>, classMirrorCreator: (KClass<*>) -> MirrorProvider<ClassMirrorInterface>): List<AnnotationMirror> {

        return annotations.map { createAnnotationMirror(it, classMirrorCreator) }
    }

    private fun createAnnotationMirror(annotation: Annotation, classMirrorCreatorCallable: ClassMirrorCreatorCallable): AnnotationMirror {
        registerAnnotationsWithServiceLoaderIfNecessary()
        require(registeredAnnotations.isNotEmpty()) {
            "No annotation was registered"
        }
        val annotationCreator = registeredAnnotations[annotation.annotationClass]

        return annotationCreator?.createAnnotationMirror(annotation, classMirrorCreatorCallable)
            ?: createAnnotationMirrorForUnregisteredAnnotation(annotation, classMirrorCreatorCallable)
    }

    private fun createAnnotationMirrorForUnregisteredAnnotation(annotation: Annotation, classMirrorCreatorCallable: ClassMirrorCreatorCallable): AnnotationMirror {
        return OtherAnnotationMirrorCreator(annotationClass = annotation.annotationClass).createAnnotationMirror(annotation, classMirrorCreatorCallable)
    }

    private fun registerAnnotationsWithServiceLoaderIfNecessary() {
        if (isServiceLoaderCalled) {
            return
        }
        synchronized(lockObject) {
            if (!isServiceLoaderCalled) {
                val annotationMirrorCreatorCallables: ServiceLoader<AnnotationMirrorCreator> = ServiceLoader.load(AnnotationMirrorCreator::class.java)
                annotationMirrorCreatorCallables.forEach {
                    registerAnnotation(it.annotationClass(), it)
                }
                isServiceLoaderCalled = true
            }
        }
    }

    private fun registerAnnotation(annotation: KClass<out Annotation>, createAnnotationMirror: AnnotationMirrorCreator) {
        require(!registeredAnnotations.containsKey(annotation)) {
            "Annotation $annotation already registered!"
        }
        registeredAnnotations[annotation] = createAnnotationMirror
    }
}