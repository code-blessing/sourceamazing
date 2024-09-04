package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FieldMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import kotlin.reflect.KProperty

data class JavaReflectionFieldMirror (
    private val kField: KProperty<*>,
): AbstractMirror(), FieldMirrorInterface {

    override val fieldName: String = kField.name
    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(kField.annotations)
    override val type: TypeMirrorInterface = JavaReflectionMirrorFactory.createTypeMirrorProvider(kField.returnType)
}