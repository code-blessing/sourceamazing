package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import java.util.*
import kotlin.reflect.KClass

object AnnotationMirrorFactory {
    private val registeredAnnotations: MutableMap<KClass<out Annotation>, AnnotationMirrorCreator> = mutableMapOf()

    fun createAnnotationMirrorList(annotations: List<Annotation>, classMirrorCreator: (KClass<*>) -> MirrorProvider<ClassMirrorInterface>): List<AnnotationMirror> {
        return annotations.map { createAnnotationMirror(it, classMirrorCreator) }
    }

    private fun createAnnotationMirror(annotation: Annotation, classMirrorCreatorCallable: ClassMirrorCreatorCallable): AnnotationMirror {
        registerAnnotationsWithServiceLoader()

        val annotationCreator = requireNotNull(registeredAnnotations[annotation.annotationClass]) {
            "Annotation ${annotation.annotationClass} is not registered. Registered classes are ${registeredAnnotations.keys}!"
        }
        return annotationCreator.createAnnotationMirror(annotation, classMirrorCreatorCallable)
    }

    private fun registerAnnotationsWithServiceLoader() {
        registeredAnnotations.clear()
        val annotationMirrorCreatorCallables: ServiceLoader<AnnotationMirrorCreator> = ServiceLoader.load(AnnotationMirrorCreator::class.java)
        annotationMirrorCreatorCallables.forEach {
            registerAnnotation(it.annotationClass(), it)
        }
    }

    private fun registerAnnotation(annotation: KClass<out Annotation>, createAnnotationMirror: AnnotationMirrorCreator) {
        require(!registeredAnnotations.containsKey(annotation)) {
            "Annotation $annotation already registered!"
        }
        registeredAnnotations[annotation] = createAnnotationMirror
    }
}