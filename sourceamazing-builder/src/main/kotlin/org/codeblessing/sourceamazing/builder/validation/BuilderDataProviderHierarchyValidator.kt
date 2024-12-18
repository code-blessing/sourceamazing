package org.codeblessing.sourceamazing.builder.validation

import org.codeblessing.sourceamazing.builder.BuilderErrorCode
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderDataProvider
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetProvidedFacetValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.interpretation.BuilderDataProviderInterpreter
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasAnnotation
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasExactNumberOfAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkHasOnlyAnnotations
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkIsNotAnnotation
import org.codeblessing.sourceamazing.schema.type.ClassCheckerUtil.checkIsNotPrivate
import org.codeblessing.sourceamazing.schema.type.returnTypeOrNull
import org.codeblessing.sourceamazing.schema.type.valueParameters
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.hasAnnotation

object BuilderDataProviderHierarchyValidator {
    private const val BUILDER_DATA_PROVIDER_CLASS_DESCRIPTION = "Builder data provider class"


    fun validateTopLevelBuilderDataProvider(builderDataProviderInterpreter: BuilderDataProviderInterpreter, schemaAccess: SchemaAccess) {
        validateBuilderDataProvider(builderDataProviderInterpreter, schemaAccess)
    }

    private fun validateBuilderDataProvider(builderDataProviderInterpreter: BuilderDataProviderInterpreter, schemaAccess: SchemaAccess) {
        val builderDataProviderClass = builderDataProviderInterpreter.dataProviderClass

        checkIsNotAnnotation(builderDataProviderClass, BUILDER_DATA_PROVIDER_CLASS_DESCRIPTION)
        checkIsNotPrivate(builderDataProviderClass, BUILDER_DATA_PROVIDER_CLASS_DESCRIPTION)
        checkHasAnnotation(BuilderDataProvider::class, builderDataProviderClass, BUILDER_DATA_PROVIDER_CLASS_DESCRIPTION)
        checkHasExactNumberOfAnnotations(BuilderDataProvider::class, builderDataProviderClass, BUILDER_DATA_PROVIDER_CLASS_DESCRIPTION, numberOf = 1)
        checkHasOnlyAnnotations(listOf(BuilderDataProvider::class), builderDataProviderClass, BUILDER_DATA_PROVIDER_CLASS_DESCRIPTION)

        builderDataProviderInterpreter.getBuilderDataMethods().forEach { builderDataMethod ->
            val builderDataMethodLocation = builderDataProviderInterpreter.builderMethodLocation(builderDataMethod)

            if(builderDataMethod.extensionReceiverParameter != null) {
                throw BuilderMethodSyntaxException(builderDataMethodLocation, BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_CAN_NOT_BE_EXTENSION_FUNCTION)
            }

            if(builderDataMethod.valueParameters().isNotEmpty()) {
                throw BuilderMethodSyntaxException(builderDataMethodLocation, BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_HAS_PARAMETERS)
            }

            if(builderDataMethod.hasAnnotation<SetProvidedFacetValue>() || builderDataMethod.hasAnnotation<SetProvidedConceptIdentifierValue>()) {
                if(builderDataMethod.returnTypeOrNull() == null) {
                    throw BuilderMethodSyntaxException(builderDataMethodLocation, BuilderErrorCode.BUILDER_DATA_PROVIDER_FUNCTION_RETURNS_NOTHING)
                }
            }
        }

        // if we have something like sub data provider, these can be validated here using something like the [RecursionDetector].

    }


}