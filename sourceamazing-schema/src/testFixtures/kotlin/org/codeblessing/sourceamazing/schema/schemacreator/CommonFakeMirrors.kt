package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKTypeParameter
import kotlin.reflect.KVariance

object CommonFakeMirrors {
    const val KOTLIN_PACKAGE_NAME = "kotlin"
    const val DEFAULT_PACKAGE_NAME = "org.codeblessing.sourceamazing.test.mock"

    fun anyClassMirror(): FakeKClass {
        return FakeKClass
            .classMirror("Any")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun conceptIdentifierClassMirror(): FakeKClass {
        return FakeKClass
            .classMirror("ConceptIdentifier")
            .withPackage("org.codeblessing.sourceamazing.schema.api")
    }

    fun listOfAnyClassMirror(): FakeKClass {
        return listOfMirror(anyClassMirror())
    }

    fun listOfMirror(innerClassMirror: FakeKClass): FakeKClass {
        // TODO Here, we return a class mirror, should probably be a TypeMirror
        return FakeKClass
            .classMirror("List")
            .withPackage(KOTLIN_PACKAGE_NAME)
            .withTypeParameter(
                FakeKTypeParameter(name = "E", variance = KVariance.INVARIANT)
            )
    }

    fun setOfMirror(innerClassMirror: FakeKClass): FakeKClass {
        // TODO Here, we return a class mirror, should probably be a TypeMirror
        return FakeKClass
            .classMirror("Set")
            .withPackage(KOTLIN_PACKAGE_NAME)
            .withTypeParameter(FakeKTypeParameter(name = "E", variance = KVariance.INVARIANT))
    }

    fun enumClassMirror(vararg enumValues: String): FakeKClass {
        return FakeKClass.enumMirror(enumValues = enumValues)
    }
    fun namedEnumClassMirror(className: String, vararg enumValues: String): FakeKClass {
        return FakeKClass.enumMirror(className = className, enumValues = enumValues)
    }

    fun stringClassMirror(): FakeKClass {
        return FakeKClass
            .classMirror("String")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun intClassMirror(): FakeKClass {
        return FakeKClass
            .classMirror("Int")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun unitClassMirror(): FakeKClass {
        return FakeKClass
            .classMirror("Unit")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

}