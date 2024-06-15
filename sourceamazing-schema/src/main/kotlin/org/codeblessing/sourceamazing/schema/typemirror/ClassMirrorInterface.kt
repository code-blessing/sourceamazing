package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass


interface ClassMirrorInterface: MirrorProvider<ClassMirrorInterface>, AbstractMirrorInterface {
    val classQualifier: ClassQualifierMirror
    val classKind: ClassKind
    override val annotations: List<AnnotationMirror>
    val methods: List<FunctionMirrorInterface>
    val fields: List<FieldMirrorInterface>
    val typeParameters: List<TypeParameterMirrorInterface>
    val superClasses: List<MirrorProvider<ClassMirrorInterface>>
    val enumValues: List<String>

    val className: String
        get() = classQualifier.className
    val packageName: String
        get() = classQualifier.packageName
    val fullQualifiedName: String
        get() = if(packageName.isNotEmpty()) "$packageName.$className" else className


    val isInterface: Boolean
        get() = classKind == ClassKind.INTERFACE
    val isClass: Boolean
        get() = classKind == ClassKind.REGULAR_CLASS
    val isObjectClass: Boolean
        get() = classKind == ClassKind.OBJECT_CLASS
    val isAnnotation: Boolean
        get() = classKind == ClassKind.ANNOTATION
    val isEnum: Boolean
        get() = classKind == ClassKind.ENUM_CLASS
    val isDataClass: Boolean
        get() = classKind == ClassKind.DATA_CLASS

    override fun provideMirror(): ClassMirrorInterface = this

    fun isClass(otherClassMirror: ClassMirrorInterface): Boolean {
        // TODO grant equality for same objects
        return this == otherClassMirror
    }

    override fun longText(): String = fullQualifiedName

    override fun shortText(): String = className

    fun convertToKClass(): KClass<*>
}