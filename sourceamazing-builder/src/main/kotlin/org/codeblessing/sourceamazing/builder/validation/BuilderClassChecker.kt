package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KClass
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberProperties
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderClassSyntaxException
import org.codeblessing.sourceamazing.builder.validation.BuilderAliasHelper.firstDuplicateAlias
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.isAnnotationClass
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.isOrdinaryInterface
import org.codeblessing.sourceamazing.schema.utils.type.KClassUtil.isPrivateClass
import org.codeblessing.sourceamazing.schema.utils.type.annotationsIncludingSuperclasses
import org.codeblessing.sourceamazing.schema.utils.type.getNumberOfAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.schema.utils.type.hasAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.schema.utils.type.isAnnotationFromSourceAmazing

class BuilderClassChecker(private val builderClassToInspect: KClass<*>) {

    fun checkIsNotAnnotation() {
        if (isAnnotationClass(builderClassToInspect)) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_BE_ANNOTATION.withFormattedMessage(),
            )
        }
    }

    fun checkIsNotPrivate() {
        if (isPrivateClass(builderClassToInspect)) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_BE_PRIVATE.withFormattedMessage(),
            )
        }
    }

    fun checkIsOrdinaryInterface() {
        if (!isOrdinaryInterface(builderClassToInspect)) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_MUST_BE_AN_INTERFACE.withFormattedMessage(),
            )
        }
    }

    fun checkHasNoGenericTypeParameters() {
        if (hasGenericTypeParameters(builderClassToInspect)) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.NO_GENERIC_TYPE_PARAMETER.withFormattedMessage(builderClassToInspect.typeParameters),
            )
        }
    }

    fun checkHasAnnotation(annotation: KClass<out Annotation>) {
        if (!builderClassToInspect.hasAnnotationIncludingSuperclasses(annotation)) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.MUST_HAVE_ANNOTATION.withFormattedMessage(annotation.annotationText()),
            )
        }
    }

    fun checkHasExactNumberOfAnnotations(annotation: KClass<out Annotation>, numberOf: Int) {
        if (builderClassToInspect.getNumberOfAnnotationIncludingSuperclasses(annotation) != numberOf) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS.withFormattedMessage(
                    numberOf,
                    annotation.annotationText(),
                ),
            )
        }
    }

    fun checkHasOnlyAnnotations(permittedAnnotations: List<KClass<out Annotation>>) {
        builderClassToInspect.annotationsIncludingSuperclasses
            .filter { it.isAnnotationFromSourceAmazing() }
            .forEach { annotationOnClass ->
                if (!permittedAnnotations.contains(annotationOnClass.annotationClass)) {
                    throw BuilderClassSyntaxException(
                        builderClassToInspect,
                        BuilderErrorCode.CAN_NOT_HAVE_ANNOTATION.withFormattedMessage(
                            annotationOnClass.annotationClass.annotationText()
                        ),
                    )
                }
            }
    }

    fun checkHasNoExtensionFunctions() {
        if (builderClassToInspect.memberExtensionFunctions.isNotEmpty()) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS.withFormattedMessage(
                    builderClassToInspect.memberExtensionFunctions
                ),
            )
        }
    }

    fun checkHasNoProperties() {
        if (builderClassToInspect.memberProperties.isNotEmpty()) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_HAVE_PROPERTIES.withFormattedMessage(
                    builderClassToInspect.memberProperties
                ),
            )
        }
    }

    fun checkAllExpectedAliasesAreProvided(expectedAliases: Set<Alias>, providedAliases: Set<Alias>) {
        expectedAliases.forEach { expectedAlias ->
            if (expectedAlias !in providedAliases) {
                throw BuilderClassSyntaxException(
                    builderClassToInspect,
                    BuilderErrorCode.ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION
                        .withFormattedMessage(expectedAlias),
                )
            }
        }
    }

    fun checkAllExpectedAliasesAreMatchingProvidedAliasOnClazzes(
        expectedAliases: Map<Alias, Clazz>,
        providedAliases: Map<Alias, Clazz>,
    ) {
        expectedAliases.forEach { (expectedAlias, expectedClazz) ->
            val providedClazz = requireNotNull(providedAliases[expectedAlias])

            if (providedClazz != expectedClazz) {
                throw BuilderClassSyntaxException(
                    builderClassToInspect,
                    BuilderErrorCode.EXPECTED_CLAZZ_FROM_SUPERIOR_BUILDER_ANNOTATION_NOT_MATCHING_PROVIDED_CLAZZ
                        .withFormattedMessage(expectedAlias, expectedClazz.simpleName(), providedClazz.simpleName()),
                )
            }
        }
    }

    fun checkNoDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation(aliasesIncludingDuplicates: List<Alias>) {
        val duplicateAlias = firstDuplicateAlias(aliasesIncludingDuplicates)

        if (duplicateAlias != null) {
            throw BuilderClassSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION
                    .withFormattedMessage(duplicateAlias),
            )
        }
    }
}
