package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.SetAsClazzModelId
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.longText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.validation.BuilderAliasHelper.firstDuplicateAlias
import org.codeblessing.sourceamazing.builder.validation.BuilderAliasHelper.firstMissingAlias
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.ClazzKind
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.utils.type.returnTypeOrNull

class BuilderMethodChecker(
    private val methodToInspect: KFunction<*>,
    private val methodLocation: MethodLocation,
    private val schemaAccess: TypeSafeSchemaAccess,
    private val clazzByAliasResolver: (Alias) -> Clazz,
) {

    fun checkHasBuilderMethodAnnotation() {
        if (!methodToInspect.hasAnnotation<BuilderMethod>()) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.MISSING_BUILDER_ANNOTATION.withFormattedMessage(),
            )
        }
    }

    fun checkNoDuplicateAliasInNewClazzAnnotation(allUsedAliasesIncludingDuplicates: List<Alias>) {
        val duplicateAlias = firstDuplicateAlias(allUsedAliasesIncludingDuplicates)

        if (duplicateAlias != null) {
            val clazz = clazzByAliasResolver(duplicateAlias)
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.ALIAS_IS_ALREADY_USED.withFormattedMessage(
                    duplicateAlias,
                    clazz.clazz.longText(),
                    allUsedAliasesIncludingDuplicates.toSet(),
                ),
            )
        }
    }

    fun checkOnlyKnownAliasesForRedeclaration(allUsedAliases: Set<Alias>, aliasRedeclarations: Map<Alias, Alias>) {
        aliasRedeclarations.forEach { (aliasToRedeclare) ->
            if (aliasToRedeclare !in allUsedAliases) {
                throw BuilderMethodSyntaxException(
                    methodLocation,
                    BuilderErrorCode.UNKNOWN_REDECLARATION_ALIAS.withFormattedMessage(
                        aliasToRedeclare,
                        allUsedAliases.toSet(),
                    ),
                )
            }
        }
    }

    fun checkNoDuplicateSetClazzModelIdAliases(setClazzModelIdValueAliases: List<Alias>) {
        val duplicateSetClazzModelIdValueAlias = firstDuplicateAlias(setClazzModelIdValueAliases)

        if (duplicateSetClazzModelIdValueAlias != null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.DUPLICATE_SET_CLAZZ_IDENTIFIER_VALUE_USAGE.withFormattedMessage(
                    duplicateSetClazzModelIdValueAlias
                ),
            )
        }
    }

    fun checkNoMissingAliasInSetClazzModelIdAnnotations(
        setClazzModelIdValueAliases: List<Alias>,
        aliasesFromNewClazzAssignment: List<Alias>,
    ) {
        firstMissingAlias(setClazzModelIdValueAliases, aliasesFromNewClazzAssignment)?.let { unknownAlias ->
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.UNKNOWN_ALIAS.withFormattedMessage(
                    unknownAlias,
                    SetAsClazzModelId::class.annotationText(),
                    aliasesFromNewClazzAssignment,
                ),
            )
        }
    }

    private fun builderReturnType(): KTypeUtil.KTypeClassInformation {
        val classesInformationFromKType =
            try {
                KTypeUtil.classesInformationFromKType(methodToInspect.returnType)
            } catch (ex: IllegalStateException) {
                throw BuilderMethodSyntaxException(
                    methodLocation,
                    BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS.withFormattedMessage(ex.message ?: ""),
                )
            }
        if (classesInformationFromKType.size != 1) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_MUST_RETURN_BUILDER_CLASS.withFormattedMessage(""),
            )
        }

        return classesInformationFromKType.first()
    }

    fun checkBuilderMethodReturnTypeIsUnitOrBuilderClass() {
        if (methodToInspect.returnTypeOrNull() == null) {
            return
        }
        builderReturnType()
    }

    fun checkBuilderMethodReturnTypeIsUnitOrNotNullable() {
        if (methodToInspect.returnTypeOrNull() == null) {
            return
        }
        val classInformation = builderReturnType()
        if (classInformation.isValueNullable) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.BUILDER_RETURNED_CAN_NOT_BE_NULLABLE.withFormattedMessage(),
            )
        }
    }

    fun checkIsKnownClazz(alias: Alias, clazz: Clazz) {
        if (schemaAccess.clazzSchemaByClazz(clazz) == null) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.UNKNOWN_CLAZZ.withFormattedMessage(alias, clazz.clazz.longText()),
            )
        }
    }

    fun checkIsInstantiableClazz(alias: Alias, clazz: Clazz) {
        val clazzSchema = schemaAccess.clazzSchemaByClazz(clazz)
        if (clazzSchema == null) {
            // this case is validated in a previous step
            return
        }
        if (clazzSchema.clazzKindInformation.clazzKind == ClazzKind.ONLY_CONSTRUCTED_INSTANCE) {
            throw BuilderMethodSyntaxException(
                methodLocation,
                BuilderErrorCode.NOT_INSTANTIATABLE_CLAZZ.withFormattedMessage(
                    alias,
                    clazz.clazz.longText(),
                    clazzSchema.clazzKindInformation.clazzKindReasons.joinToString("\n") { it.message },
                ),
            )
        }
    }
}
