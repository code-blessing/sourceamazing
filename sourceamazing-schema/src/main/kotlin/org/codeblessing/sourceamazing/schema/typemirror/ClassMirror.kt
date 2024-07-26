package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass


data class ClassMirror(
    val classQualifier: ClassQualifierMirror,
    val classKind: ClassKind = ClassKind.REGULAR_CLASS,
    override val annotations: List<AnnotationMirror> = emptyList(),
    val methods: List<FunctionMirror> = emptyList(),
    val propertiesNames: List<String> = emptyList(),
    val typeParameters: List<MirrorProvider<ClassMirror>> = emptyList(),
    val superClasses: List<MirrorProvider<ClassMirror>> = emptyList(),
    val enumValues: List<String> = emptyList(),
): AbstractMirror(), MirrorProvider<ClassMirror>, SignatureMirror {

    companion object {
        fun classMirror(className: String = "UnnamedClass", packageName: String = ""): ClassMirror{
            return ClassMirror(
                classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
            )
        }
        fun interfaceMirror(className: String = "UnnamedInterface", packageName: String = ""): ClassMirror {
            return classMirror(className = className, packageName = packageName).setIsInterface()
        }
        fun enumMirror(className: String = "UnnamedEnum", packageName: String = "", vararg enumValues: String): ClassMirror{
            return ClassMirror(
                classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
                enumValues = enumValues.toList()
            ).setIsEnum()
        }
    }

    val className: String = classQualifier.className
    val packageName: String = classQualifier.packageName
    val fullQualifiedName: String = if(packageName.isNotEmpty()) "$packageName.$className" else className


    val isInterface: Boolean = classKind == ClassKind.INTERFACE
    val isClass: Boolean = classKind == ClassKind.REGULAR_CLASS
    val isObjectClass: Boolean = classKind == ClassKind.OBJECT_CLASS
    val isAnnotation: Boolean = classKind == ClassKind.ANNOTATION
    val isEnum: Boolean = classKind == ClassKind.ENUM_CLASS
    val isDataClass: Boolean = classKind == ClassKind.DATA_CLASS

    override fun provideMirror(): ClassMirror = this

    override fun longText(): String = fullQualifiedName

    override fun shortText(): String = className

    fun isClass(clazz: KClass<*>): Boolean {
        return false // TODO implement
    }

    fun setIsEnum(): ClassMirror {
        return this.copy(
            classKind = ClassKind.ENUM_CLASS,
        )
    }

    fun setIsAnnotation(): ClassMirror {
        return this.copy(
            classKind = ClassKind.ANNOTATION,
        )
    }


    fun withAnnotation(annotation: AnnotationMirror): ClassMirror {
        return this.copy(
            annotations = this.annotations + annotation
        )
    }

    fun withClassName(className: String): ClassMirror {
        return this.copy(
            classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
        )
    }

    fun withPackage(packageName: String): ClassMirror {
        return this.copy(
            classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
        )
    }

    fun setIsInterface(): ClassMirror {
        return this.copy(
            classKind = ClassKind.INTERFACE,
        )
    }

    fun setIsClass(): ClassMirror {
        return this.copy(
            classKind = ClassKind.REGULAR_CLASS,
        )
    }

    fun setIsObjectClass(): ClassMirror {
        return this.copy(
            classKind = ClassKind.OBJECT_CLASS,
        )
    }

    fun setIsDataClass(): ClassMirror {
        return this.copy(
            classKind = ClassKind.DATA_CLASS,
        )
    }

    fun withTypeParameter(classMirror: ClassMirror): ClassMirror {
        return this.copy(
            typeParameters = this.typeParameters + classMirror
        )
    }

    fun withSuperClass(classMirror: ClassMirror): ClassMirror {
        return this.copy(
            superClasses = this.superClasses + classMirror
        )
    }

    fun withMethod(method: FunctionMirror): ClassMirror {
        return this.copy(
            methods = this.methods + method
        )
    }
}