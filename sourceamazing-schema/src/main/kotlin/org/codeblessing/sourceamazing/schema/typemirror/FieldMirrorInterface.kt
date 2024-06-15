package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider

interface FieldMirrorInterface: MirrorProvider<FieldMirrorInterface>, AbstractMirrorInterface {
    val fieldName: String
    override val annotations: List<AnnotationMirror>
    val type: TypeMirrorInterface

    override fun provideMirror(): FieldMirrorInterface = this

    override fun longText(): String = fieldName

    override fun shortText(): String  = fieldName
}