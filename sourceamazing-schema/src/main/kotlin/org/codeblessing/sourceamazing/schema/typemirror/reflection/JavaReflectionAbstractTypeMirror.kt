package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirrorInterface
import kotlin.reflect.KType


abstract class JavaReflectionAbstractTypeMirror(
    kType: KType,
): AbstractMirror(), TypeMirrorInterface, AbstractMirrorInterface {
    private val description = kType.toString()

    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(kType.annotations)
    override val nullable: Boolean = kType.isMarkedNullable

    override fun provideMirror(): TypeMirrorInterface = this
    override fun longText(): String = description

    override fun shortText(): String = description
}