package org.codeblessing.sourceamazing.schema.typemirror

import kotlin.reflect.KClass


data class ClassMirror(
    val className: String,
    val packageName: String = "",
    val isInterface: Boolean = false,
    val isClass: Boolean = true,
    val isObjectClass: Boolean = false,
    val isAnnotation: Boolean = false,
    val isEnum: Boolean = false,
    override val annotations: List<AnnotationMirror> = emptyList(),
    val methods: List<MethodMirror> = emptyList(),
    val propertiesNames: List<String> = emptyList(),
    val typeParameters: List<ClassMirror> = emptyList(),
    val superClasses: List<ClassMirror> = emptyList(),
    val enumValues: List<String> = emptyList(),
): AbstractMirror() {

    companion object {
        fun classMirror(className: String = "UnnamedClass"): ClassMirror{
            return ClassMirror(
                className = className
            )
        }
        fun interfaceMirror(className: String = "UnnamedInterface"): ClassMirror {
            return classMirror(className).setIsInterface()
        }
        fun enumMirror(className: String = "UnnamedEnum", vararg enumValues: String): ClassMirror{
            return ClassMirror(
                className = className,
                enumValues = enumValues.toList()
            ).setIsEnum()
        }
    }

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
            className = className
        )
    }

    fun withPackage(packageName: String): ClassMirror {
        return this.copy(
            packageName = packageName
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

    fun withMethod(method: MethodMirror): ClassMirror {
        return this.copy(
            methods = this.methods + method
        )
    }
}