package org.codeblessing.sourceamazing.builder.factories

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.isSuperclassOf
import org.codeblessing.sourceamazing.builder.api.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.BuilderFactory
import org.codeblessing.sourceamazing.builder.exceptions.BuilderFactoryException
import org.codeblessing.sourceamazing.builder.validation.BuilderAnnotationValidationUtil.hasBuilderAnnotation

object BuilderFactoriesValidator {
    fun validateBuilderFactories(builderFactories: BuilderFactoriesHolder) {
        builderFactories.builderFactories.forEach { factory ->
            checkHasNoDuplicateBuilderClasses(factory, builderFactories)
            checkBuilderImplInheritsFromBuilder(factory)
            checkIsOrdinaryImplementationClass(factory)
            checkBuilderImplCanBeInstantiated(factory)
            checkBuilderImplHasNoBuilderAnnotations(factory)
        }
    }

    fun checkHasNoDuplicateBuilderClasses(factory: BuilderFactory<*, *>, builderFactories: BuilderFactoriesHolder) {
        val numberOfBuilders = builderFactories.builderFactories.count { it.builderClass == factory.builderClass }

        if (numberOfBuilders > 1) {
            throw BuilderFactoryException(
                factory,
                BuilderErrorCode.BUILDER_FACTORY_DUPLICATES_FOUND.withFormattedMessage(factory.builderClass),
            )
        }
    }

    fun checkBuilderImplInheritsFromBuilder(factory: BuilderFactory<*, *>) {
        if (!factory.builderClass.isSuperclassOf(factory.builderImplementationClass)) {
            throw BuilderFactoryException(
                factory,
                BuilderErrorCode.BUILDER_IMPLEMENTATION_MUST_INHERIT_FROM_BUILDER_INTERFACE.withFormattedMessage(),
            )
        }
    }

    fun checkIsOrdinaryImplementationClass(factory: BuilderFactory<*, *>) {
        if (factory.builderImplementationSupplier != null) {
            // instantiation is done by supplier
            return
        }

        val builderImplementationClass = factory.builderImplementationClass
        if (
            builderImplementationClass.isAbstract ||
                builderImplementationClass.isSealed ||
                builderImplementationClass.isInner ||
                builderImplementationClass.isCompanion ||
                builderImplementationClass.java.isInterface ||
                builderImplementationClass.java.isAnnotation
        ) {
            throw BuilderFactoryException(
                factory,
                BuilderErrorCode.BUILDER_IMPLEMENTATION_MUST_NOT_BE_ABSTRACT.withFormattedMessage(),
            )
        }
    }

    fun checkBuilderImplCanBeInstantiated(factory: BuilderFactory<*, *>) {
        if (factory.builderImplementationSupplier != null) {
            // instantiation is done by supplier
            return
        }

        val builderImplementationClass = factory.builderImplementationClass

        if (
            builderImplementationClass.constructors.isEmpty() ||
                builderImplementationClass.constructors.none {
                    BuilderFactoryConstructorUtil.isValidConstructor(it, factory.builderClass)
                }
        ) {
            throw BuilderFactoryException(
                factory,
                BuilderErrorCode.BUILDER_IMPLEMENTATION_MUST_HAVE_A_VALID_CONSTRUCTOR.withFormattedMessage(),
            )
        }
    }

    private fun checkBuilderImplHasNoBuilderAnnotations(factory: BuilderFactory<*, *>) {
        val builderImplementationClass = factory.builderImplementationClass
        checkHasNoBuilderAnnotations(builderImplementationClass, factory)

        // unfortunately, we cannot check that none of these builder annotations are
        // on the implementation methods and method parameters, as otherwise the
        // kotlin delegation (https://kotlinlang.org/docs/delegation.html) would
        // not be possible anymore.
    }

    private fun checkHasNoBuilderAnnotations(element: KAnnotatedElement, factory: BuilderFactory<*, *>) {
        if (hasBuilderAnnotation(element)) {
            throw BuilderFactoryException(
                factory,
                BuilderErrorCode.BUILDER_IMPLEMENTATION_CAN_NOT_HAVE_BUILDER_ANNOTATIONS.withFormattedMessage(element),
            )
        }
    }
}
