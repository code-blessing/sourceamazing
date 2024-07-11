package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror

object CommonMirrors {
    const val KOTLIN_PACKAGE_NAME = "kotlin"
    const val DEFAULT_PACKAGE_NAME = "org.codeblessing.sourceamazing.test.mock"

    fun anyClassMirror(): ClassMirror {
        return ClassMirror
            .classMirror("Any")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun conceptIdentifierClassMirror(): ClassMirror {
        return ClassMirror
            .classMirror("ConceptIdentifier")
            .withPackage("org.codeblessing.sourceamazing.schema.api")
    }

    fun listOfAnyClassMirror(): ClassMirror {
        return listOfMirror(anyClassMirror())
    }

    fun listOfMirror(innerClassMirror: ClassMirror): ClassMirror {
        return ClassMirror
            .classMirror("List")
            .withPackage(KOTLIN_PACKAGE_NAME)
            .withTypeParameter(innerClassMirror)
    }

    fun setOfMirror(innerClassMirror: ClassMirror): ClassMirror {
        return ClassMirror
            .classMirror("Set")
            .withPackage(KOTLIN_PACKAGE_NAME)
            .withTypeParameter(innerClassMirror)
    }

    fun enumClassMirror(vararg enumValues: String): ClassMirror {
        return ClassMirror.enumMirror(enumValues = enumValues)
    }
    fun namedEnumClassMirror(className: String, vararg enumValues: String): ClassMirror {
        return ClassMirror.enumMirror(className = className, enumValues = enumValues)
    }

    fun stringClassMirror(): ClassMirror {
        return ClassMirror
            .classMirror("String")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun intClassMirror(): ClassMirror {
        return ClassMirror
            .classMirror("Int")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

    fun unitClassMirror(): ClassMirror {
        return ClassMirror
            .classMirror("Unit")
            .withPackage(KOTLIN_PACKAGE_NAME)
    }

}