package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KFunction
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderDataProvider
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.builder.interpretation.BuilderDataProviderInterpreter

object BuilderDataProviderHierarchyValidator {

    fun validateTopLevelBuilderDataProvider(builderDataProviderInterpreter: BuilderDataProviderInterpreter) {
        validateBuilderDataProvider(builderDataProviderInterpreter)
    }

    private fun validateBuilderDataProvider(builderDataProviderInterpreter: BuilderDataProviderInterpreter) {
        val builderClassChecker = builderClassChecker(builderDataProviderInterpreter)
        with(builderClassChecker) {
            checkIsNotAnnotation()
            checkIsNotPrivate()
            checkHasAnnotation(BuilderDataProvider::class)
            checkHasExactNumberOfAnnotations(BuilderDataProvider::class, numberOf = 1)
            checkHasOnlyAnnotations(listOf(BuilderDataProvider::class))
        }

        builderDataProviderInterpreter.getBuilderDataMethods().forEach { builderDataMethod ->
            val dataProviderMethodChecker = dataProviderMethodChecker(builderDataMethod, builderDataProviderInterpreter)

            with(dataProviderMethodChecker) {
                checkHasNoExtensionReceiverParameter()
                checkHasNoValueParameters()
                checkHasReturnTypeIfHasAnnotation(SetProvidedFacetValue::class)
                checkHasReturnTypeIfHasAnnotation(SetProvidedConceptIdentifierValue::class)
            }
        }

        // if we have something like sub data provider, these can be validated here using something
        // like the [RecursionDetector].

    }

    private fun builderClassChecker(
        builderDataProviderInterpreter: BuilderDataProviderInterpreter
    ): BuilderClassChecker {
        return BuilderClassChecker(builderDataProviderInterpreter.dataProviderClass, "Builder data provider class")
    }

    private fun dataProviderMethodChecker(
        builderDataMethod: KFunction<*>,
        builderDataProviderInterpreter: BuilderDataProviderInterpreter,
    ): DataProviderMethodChecker {
        val methodLocation = builderDataProviderInterpreter.builderMethodLocation(builderDataMethod)
        return DataProviderMethodChecker(builderDataMethod, methodLocation)
    }
}
