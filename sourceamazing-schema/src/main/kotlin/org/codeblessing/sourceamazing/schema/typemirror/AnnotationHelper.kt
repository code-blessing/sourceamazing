package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass

object AnnotationHelper {

    fun createAnnotationList(annotations: List<Annotation>, classMirrorCreator: (KClass<*>) -> MirrorProvider<ClassMirrorInterface>): List<AnnotationMirror> {
        return annotations
            .mapNotNull { annotation: Annotation ->
                when(annotation) {
                    is Schema -> createSchemaAnnotationMirror(annotation, classMirrorCreator)
                    is Concept -> createConceptAnnotationMirror(annotation, classMirrorCreator)
                    // TODO all other annotations
                    else -> null
                }
            }
    }

    private fun createSchemaAnnotationMirror(schemaAnnotation: Schema, classMirrorCreator: (KClass<*>) -> MirrorProvider<ClassMirrorInterface>): SchemaAnnotationMirror {
        return SchemaAnnotationMirror(schemaAnnotation.concepts.map(classMirrorCreator))
    }

    private fun createConceptAnnotationMirror(conceptAnnotation: Concept, classMirrorCreator: (KClass<*>) -> MirrorProvider<ClassMirrorInterface>): ConceptAnnotationMirror {
        return ConceptAnnotationMirror(conceptAnnotation.facets.map(classMirrorCreator))
    }

}