package org.codeblessing.sourceamazing.schema.fakereflection

import kotlin.reflect.KAnnotatedElement

abstract class FakeKAnnotatedElement<T: FakeKAnnotatedElement<T>>() : KAnnotatedElement {
    private val internalAnnotations: MutableList<Annotation> = mutableListOf()

    override val annotations: List<Annotation> = internalAnnotations

    fun withAnnotation(annotation: Annotation): T {
        internalAnnotations.add(annotation)
        return castThisToSubtype()
    }

    fun withAnnotations(vararg annotations: Annotation): T {
        internalAnnotations.addAll(annotations)
        return castThisToSubtype()
    }

    private fun castThisToSubtype(): T {
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

}