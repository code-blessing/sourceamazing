package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass


data class ClassMirror(
    val classQualifier: ClassQualifierMirror,
    val isInterface: Boolean = false,
    val isClass: Boolean = true,
    val isObjectClass: Boolean = false,
    val isAnnotation: Boolean = false,
    val isEnum: Boolean = false,
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

    override fun provideMirror(): ClassMirror = this

    override fun longText(): String = fullQualifiedName

    override fun shortText(): String = className

    fun isClass(clazz: KClass<*>): Boolean {
        return false // TODO implement
    }

    fun setIsEnum(): ClassMirror {
        return this.copy(
            isEnum = true,
            isClass = false,
        )
    }

    fun setIsAnnotation(): ClassMirror {
        return this.copy(
            isAnnotation = true,
            isClass = false,
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
            isInterface = true,
            isClass = false,
        )
    }

    fun setIsClass(): ClassMirror {
        return this.copy(
            isInterface = false,
            isClass = true,
        )
    }

    fun setIsObjectClass(): ClassMirror {
        return this.copy(
            isInterface = false,
            isClass = true,
            isObjectClass = true,
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