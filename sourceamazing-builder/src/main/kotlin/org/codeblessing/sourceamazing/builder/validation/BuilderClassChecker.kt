package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.builder.validation.BuilderAliasHelper.firstDuplicateAlias
import org.codeblessing.sourceamazing.utils.type.KClassUtil.hasGenericTypeParameters
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isAnnotationClass
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isOrdinaryInterface
import org.codeblessing.sourceamazing.utils.type.KClassUtil.isPrivateClass
import org.codeblessing.sourceamazing.utils.type.annotationsIncludingSuperclasses
import org.codeblessing.sourceamazing.utils.type.getNumberOfAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.utils.type.hasAnnotationIncludingSuperclasses
import org.codeblessing.sourceamazing.utils.type.isAnnotationFromSourceAmazing
import kotlin.reflect.KClass
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberProperties

class BuilderClassChecker(private val builderClassToInspect: KClass<*>, private val classDescription: String) {

    fun checkIsNotAnnotation() {
        if (isAnnotationClass(builderClassToInspect)) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_BE_ANNOTATION,
                classDescription,
            )
        }
    }

    fun checkIsNotPrivate() {
        if (isPrivateClass(builderClassToInspect)) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_BE_PRIVATE,
                classDescription,
            )
        }
    }

    fun checkIsOrdinaryInterface() {
        if (!isOrdinaryInterface(builderClassToInspect)) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_MUST_BE_AN_INTERFACE,
                classDescription,
            )
        }
    }

    fun checkHasNoGenericTypeParameters() {
        if (hasGenericTypeParameters(builderClassToInspect)) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.NO_GENERIC_TYPE_PARAMETER,
                classDescription,
                builderClassToInspect.typeParameters,
            )
        }
    }

    fun checkHasAnnotation(annotation: KClass<out Annotation>) {
        if (!builderClassToInspect.hasAnnotationIncludingSuperclasses(annotation)) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.MUST_HAVE_ANNOTATION,
                classDescription,
                annotation.annotationText(),
            )
        }
    }

    fun checkHasExactNumberOfAnnotations(annotation: KClass<out Annotation>, numberOf: Int) {
        if (builderClassToInspect.getNumberOfAnnotationIncludingSuperclasses(annotation) != numberOf) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.NOT_MORE_THAN_NUMBER_OF_ANNOTATIONS,
                classDescription,
                numberOf,
                annotation.annotationText(),
            )
        }
    }

    fun checkHasOnlyAnnotations(permittedAnnotations: List<KClass<out Annotation>>) {
        builderClassToInspect.annotationsIncludingSuperclasses
            .filter { it.isAnnotationFromSourceAmazing() }
            .forEach { annotationOnClass ->
                if (!permittedAnnotations.contains(annotationOnClass.annotationClass)) {
                    throw BuilderSyntaxException(
                        builderClassToInspect,
                        BuilderErrorCode.CAN_NOT_HAVE_ANNOTATION,
                        classDescription,
                        annotationOnClass.annotationClass.annotationText(),
                    )
                }
            }
    }

    fun checkHasNoExtensionFunctions() {
        if (builderClassToInspect.memberExtensionFunctions.isNotEmpty()) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_HAVE_EXTENSION_FUNCTIONS,
                classDescription,
                builderClassToInspect.memberExtensionFunctions,
            )
        }
    }

    fun checkHasNoProperties() {
        if (builderClassToInspect.memberProperties.isNotEmpty()) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.CLASS_CANNOT_HAVE_PROPERTIES,
                classDescription,
                builderClassToInspect.memberProperties,
            )
        }
    }

    fun checkAllExpectedAliasesAreProvided(expectedAliases: Set<Alias>, providedAliases: Set<Alias>) {
        expectedAliases.forEach { expectedAlias ->
            if (expectedAlias !in providedAliases) {
                throw BuilderSyntaxException(
                    builderClassToInspect,
                    BuilderErrorCode.ALIAS_NO_AVAILABLE_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION,
                    expectedAlias,
                )
            }
        }
    }

    fun checkNoDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation(aliasesIncludingDuplicates: List<Alias>) {
        val duplicateAlias = firstDuplicateAlias(aliasesIncludingDuplicates)

        if (duplicateAlias != null) {
            throw BuilderSyntaxException(
                builderClassToInspect,
                BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION,
                duplicateAlias,
            )
        }
    }
}
