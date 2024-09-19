package org.codeblessing.sourceamazing.schema.fakereflection

import org.codeblessing.sourceamazing.schema.type.ClassNameUtil
import org.codeblessing.sourceamazing.schema.type.KClassJavaCompatibilityLayer
import org.codeblessing.sourceamazing.schema.typemirror.ClassKind
import org.codeblessing.sourceamazing.schema.typemirror.FakeTypeParameterMirror
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility

class FakeKClass(): FakeKAnnotatedElement<FakeKClass>(), KClass<Any>, KClassJavaCompatibilityLayer {
    private var internalSimpleName: String? = null
    private var internalPackageName: String? = null
    private var classKind: ClassKind = ClassKind.REGULAR_CLASS
    private val internalMembers: MutableList<KCallable<*>> = mutableListOf()
    private val internalSupertypes: MutableList<KType> = mutableListOf()
    private val internalEnumValues: MutableList<String> = mutableListOf()

    override val constructors: Collection<KFunction<Any>> = emptyList()
    override val isAbstract: Boolean = false
    override val isCompanion: Boolean = false
    override val isData: Boolean get() = classKind == ClassKind.DATA_CLASS
    override val isFinal: Boolean = true
    override val isFun: Boolean = false
    override val isInner: Boolean = false
    override val isOpen: Boolean = false
    override val isSealed: Boolean = false
    override val isValue: Boolean = false
    override val members: Collection<KCallable<*>> = internalMembers
    override val nestedClasses: Collection<KClass<*>> = emptyList()
    override val objectInstance: Any? = null
    override val qualifiedName: String? get() = ClassNameUtil.fullQualifiedName(internalPackageName, internalSimpleName)
    override val sealedSubclasses: List<KClass<out Any>> = emptyList()
    override val simpleName: String? get() = internalSimpleName
    override val supertypes: List<KType> = internalSupertypes
    override val typeParameters: List<KTypeParameter> = emptyList()
    override val visibility: KVisibility? = null

    override val enumValues: List<String> get() = internalEnumValues
    override val isInterface: Boolean get() = classKind == ClassKind.INTERFACE
    override val isEnum: Boolean get() = classKind == ClassKind.ENUM_CLASS
    override val isAnnotation: Boolean get() = classKind == ClassKind.ANNOTATION
    override val isRegularClass: Boolean get() = classKind == ClassKind.REGULAR_CLASS

    override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        TODO("Not yet implemented")
    }

    override fun isInstance(value: Any?): Boolean {
        TODO("Not yet implemented")
    }

    companion object {
        // TODO rename method names
        fun classMirror(className: String = "UnnamedClass", packageName: String = ""): FakeKClass {
            return FakeKClass().withClassName(className).withPackage(packageName)
        }
        fun interfaceMirror(className: String = "UnnamedInterface", packageName: String = ""): FakeKClass {
            return classMirror(className = className, packageName = packageName).setIsInterface()
        }
        fun enumMirror(className: String = "UnnamedEnum", packageName: String = "", vararg enumValues: String): FakeKClass {
            return classMirror(className = className, packageName = packageName).setIsEnum().setEnumValues(*enumValues)
        }
    }

    fun setIsEnum(): FakeKClass {
        classKind = ClassKind.ENUM_CLASS
        return this
    }

    fun setEnumValues(vararg enumValues: String): FakeKClass {
        internalEnumValues.clear()
        internalEnumValues.addAll(enumValues)
        return this
    }

    fun setIsAnnotation(): FakeKClass {
        classKind = ClassKind.ANNOTATION
        return this
    }

    fun withClassName(className: String): FakeKClass {
        internalSimpleName = className
        return this
    }

    fun withPackage(packageName: String): FakeKClass {
        internalPackageName = packageName
        return this
    }

    fun setIsInterface(): FakeKClass {
        classKind = ClassKind.INTERFACE
        return this
    }

    fun setIsClass(): FakeKClass {
        classKind = ClassKind.REGULAR_CLASS
        return this
    }

    fun setIsObjectClass(): FakeKClass {
        classKind = ClassKind.OBJECT_CLASS
        return this
    }

    fun setIsDataClass(): FakeKClass {
        classKind = ClassKind.DATA_CLASS
        return this
    }

    fun withTypeParameter(typeMirror: FakeKTypeParameter): FakeKClass {
        TODO("Not yet implemented")
    }

    fun withSuperClass(clazz: KClass<*>): FakeKClass {
        TODO("Not yet implemented")
    }

    fun withMember(kCallable: KCallable<*>): FakeKClass {
        internalMembers.add(kCallable)
        return this
    }

    fun withMethod(method: KCallable<*>): FakeKClass = withMember(method)
}