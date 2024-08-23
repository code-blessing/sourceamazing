package org.codeblessing.sourceamazing.schema.typemirror.reflection

import org.codeblessing.sourceamazing.schema.typemirror.AbstractMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassKind
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.ClassQualifierMirror
import org.codeblessing.sourceamazing.schema.typemirror.FieldMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.OtherTypeMirrorInterface
import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties


data class JavaReflectionOtherTypeMirror(
    private val kType: KType,
): AbstractMirror(), OtherTypeMirrorInterface {

    override val annotations: List<AnnotationMirror> = JavaReflectionMirrorFactory.createAnnotationList(kType.annotations)

    override fun provideMirror(): OtherTypeMirrorInterface = this
    override fun longText(): String {
        return kType.toString()
    }

    override fun shortText(): String {
        return kType.toString()
    }
}