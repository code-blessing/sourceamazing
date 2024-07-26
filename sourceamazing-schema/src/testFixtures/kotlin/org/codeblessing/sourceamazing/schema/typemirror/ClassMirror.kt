package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass


data class ClassMirror(
    override val classQualifier: ClassQualifierMirror,
    override val classKind: ClassKind = ClassKind.REGULAR_CLASS,
    override val annotations: List<AnnotationMirror> = emptyList(),
    override val methods: List<FunctionMirror> = emptyList(),
    override val propertiesNames: List<String> = emptyList(),
    override val typeParameters: List<MirrorProvider<ClassMirrorInterface>> = emptyList(),
    override val superClasses: List<MirrorProvider<ClassMirrorInterface>> = emptyList(),
    override val enumValues: List<String> = emptyList(),
): AbstractMirror(), ClassMirrorInterface {

    companion object {
        fun classMirror(className: String = "UnnamedClass", packageName: String = ""): ClassMirror {
            return ClassMirror(
                classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
            )
        }
        fun interfaceMirror(className: String = "UnnamedInterface", packageName: String = ""): ClassMirror {
            return classMirror(className = className, packageName = packageName).setIsInterface()
        }
        fun enumMirror(className: String = "UnnamedEnum", packageName: String = "", vararg enumValues: String): ClassMirror {
            return ClassMirror(
                classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
                enumValues = enumValues.toList()
            ).setIsEnum()
        }
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