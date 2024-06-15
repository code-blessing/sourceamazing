package org.codeblessing.sourceamazing.schema.typemirror

import org.codeblessing.sourceamazing.schema.typemirror.provider.MirrorProvider
import kotlin.reflect.KClass


data class FakeClassMirror(
    override val classQualifier: ClassQualifierMirror,
    override val classKind: ClassKind = ClassKind.REGULAR_CLASS,
    override val annotations: List<AnnotationMirror> = emptyList(),
    override val methods: List<FakeFunctionMirror> = emptyList(),
    override val fields: List<FieldMirrorInterface> = emptyList(),
    override val typeParameters: List<TypeParameterMirrorInterface> = emptyList(),
    override val superClasses: List<MirrorProvider<ClassMirrorInterface>> = emptyList(),
    override val enumValues: List<String> = emptyList(),
): AbstractMirror(), ClassMirrorInterface {

    companion object {
        fun classMirror(className: String = "UnnamedClass", packageName: String = ""): FakeClassMirror {
            return FakeClassMirror(
                classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
            )
        }
        fun interfaceMirror(className: String = "UnnamedInterface", packageName: String = ""): FakeClassMirror {
            return classMirror(className = className, packageName = packageName).setIsInterface()
        }
        fun enumMirror(className: String = "UnnamedEnum", packageName: String = "", vararg enumValues: String): FakeClassMirror {
            return FakeClassMirror(
                classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
                enumValues = enumValues.toList()
            ).setIsEnum()
        }
    }

    override fun convertToKClass(): KClass<*> {
        throw UnsupportedOperationException("Can not create a KClass from a class mirror mock")
    }

    fun setIsEnum(): FakeClassMirror {
        return this.copy(
            classKind = ClassKind.ENUM_CLASS,
        )
    }

    fun setEnumValues(vararg enumValues: String): FakeClassMirror {
        return this.copy(
            enumValues = enumValues.toList()
        )
    }

    fun setIsAnnotation(): FakeClassMirror {
        return this.copy(
            classKind = ClassKind.ANNOTATION,
        )
    }

    fun withAnnotation(annotation: AnnotationMirror): FakeClassMirror {
        return this.copy(
            annotations = this.annotations + annotation
        )
    }

    fun withClassName(className: String): FakeClassMirror {
        return this.copy(
            classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
        )
    }

    fun withPackage(packageName: String): FakeClassMirror {
        return this.copy(
            classQualifier = ClassQualifierMirror(className = className, packageName = packageName),
        )
    }

    fun setIsInterface(): FakeClassMirror {
        return this.copy(
            classKind = ClassKind.INTERFACE,
        )
    }

    fun setIsClass(): FakeClassMirror {
        return this.copy(
            classKind = ClassKind.REGULAR_CLASS,
        )
    }

    fun setIsObjectClass(): FakeClassMirror {
        return this.copy(
            classKind = ClassKind.OBJECT_CLASS,
        )
    }

    fun setIsDataClass(): FakeClassMirror {
        return this.copy(
            classKind = ClassKind.DATA_CLASS,
        )
    }

    fun withTypeParameter(typeMirror: FakeTypeParameterMirror): FakeClassMirror {
        return this.copy(
            typeParameters = this.typeParameters + typeMirror
        )
    }

    fun withSuperClass(classMirror: FakeClassMirror): FakeClassMirror {
        return this.copy(
            superClasses = this.superClasses + classMirror
        )
    }

    fun withMethod(method: FakeFunctionMirror): FakeClassMirror {
        return this.copy(
            methods = this.methods + method
        )
    }
}