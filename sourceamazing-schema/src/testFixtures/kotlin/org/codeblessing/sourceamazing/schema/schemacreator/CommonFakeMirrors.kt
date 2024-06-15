package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeTypeParameterMirror

object CommonFakeMirrors {
    const val KOTLIN_PACKAGE_NAME = "kotlin"
    const val DEFAULT_PACKAGE_NAME = "org.codeblessing.sourceamazing.test.mock"

    fun anyClassMirror(): FakeClassMirror {
        return FakeClassMirror
            .classMirror("Any")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun conceptIdentifierClassMirror(): FakeClassMirror {
        return FakeClassMirror
            .classMirror("ConceptIdentifier")
            .withPackage("org.codeblessing.sourceamazing.schema.api")
    }

    fun listOfAnyClassMirror(): FakeClassMirror {
        return listOfMirror(anyClassMirror())
    }

    fun listOfMirror(innerClassMirror: FakeClassMirror): FakeClassMirror {
        // TODO Here, we return a class mirror, should probably be a TypeMirror
        return FakeClassMirror
            .classMirror("List")
            .withPackage(KOTLIN_PACKAGE_NAME)
            .withTypeParameter(
                FakeTypeParameterMirror(name = "E")
            )
    }

    fun setOfMirror(innerClassMirror: FakeClassMirror): FakeClassMirror {
        // TODO Here, we return a class mirror, should probably be a TypeMirror
        return FakeClassMirror
            .classMirror("Set")
            .withPackage(KOTLIN_PACKAGE_NAME)
            .withTypeParameter(FakeTypeParameterMirror(name = "E"))
    }

    fun enumClassMirror(vararg enumValues: String): FakeClassMirror {
        return FakeClassMirror.enumMirror(enumValues = enumValues)
    }
    fun namedEnumClassMirror(className: String, vararg enumValues: String): FakeClassMirror {
        return FakeClassMirror.enumMirror(className = className, enumValues = enumValues)
    }

    fun stringClassMirror(): FakeClassMirror {
        return FakeClassMirror
            .classMirror("String")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun intClassMirror(): FakeClassMirror {
        return FakeClassMirror
            .classMirror("Int")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun unitClassMirror(): FakeClassMirror {
        return FakeClassMirror
            .classMirror("Unit")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

}