package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import kotlin.reflect.full.*
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongClassStructureSyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.WrongPropertySyntaxException
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasMemberExtensionFunctions
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasMemberFunctions
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isOrdinaryInterface

object ConceptClassValidator {

    fun validateConceptClass(definitionClass: KClass<*>) {
        if (!isOrdinaryInterface(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.CLASS_MUST_BE_AN_INTERFACE,
                definitionClass,
            )
        }
        if (hasGenericTypeParameters(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.NO_GENERIC_TYPE_PARAMETER,
                definitionClass.longText(),
                definitionClass.typeParameters,
            )
        }

        if (hasMemberExtensionFunctions(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS,
                definitionClass,
                definitionClass.memberExtensionFunctions,
            )
        }
        if (hasMemberFunctions(definitionClass)) {
            throw WrongClassStructureSyntaxException(
                definitionClass,
                SchemaErrorCode.CLASS_CANNOT_HAVE_MEMBER_FUNCTIONS,
                definitionClass,
                definitionClass.memberFunctions,
            )
        }

        val extensionProperty = definitionClass.memberExtensionProperties.firstOrNull()
        if (extensionProperty != null) {
            throw WrongPropertySyntaxException(extensionProperty, SchemaErrorCode.PROPERTY_MUST_NOT_HAVE_EXTENSION_TYPE)
        }
    }

    private fun KClass<*>.longText(): String {
        return java.name
    }
}
