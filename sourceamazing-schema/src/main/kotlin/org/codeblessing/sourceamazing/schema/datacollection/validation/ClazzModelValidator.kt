package org.codeblessing.sourceamazing.schema.datacollection.validation

import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.*
import org.codeblessing.sourceamazing.schema.datacollection.validation.DataValidationFailureCollector.Companion.collectAndThrowExceptionOnValidationFailures
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.ClazzKind
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

object ClazzModelValidator {

    @Throws(DataValidationException::class)
    fun validateClazzForNewClazzModel(
        schema: TypeSafeSchemaAccess,
        clazz: Clazz,
        clazzModelId: ClazzModelId,
        allClazzModelIds: Set<ClazzModelId>,
    ) {
        return collectAndThrowExceptionOnValidationFailures { validationFailureCollector ->
            val checks = ValidationChecks(validationFailureCollector)
            checks.checkIsKnownClazz(schema, clazz)
            checks.checkIsInstantiableClazz(schema, clazz)
            checks.checkIsNotDuplicateClazzModelId(allClazzModelIds, clazz, clazzModelId)
        }
    }

    @Throws(DataValidationException::class)
    fun validateEntries(
        schema: TypeSafeSchemaAccess,
        clazzModelDataEntries: List<TypeSafeClazzModel>,
    ): Map<ClazzModelId, TypeSafeClazzModel> {
        return collectAndThrowExceptionOnValidationFailures { validationFailureCollector ->
            val validData: Map<ClazzModelId, TypeSafeClazzModel> =
                validateClazzAndIdentifier(schema, clazzModelDataEntries, validationFailureCollector)

            ClazzPropertyDataValidator.validateClazzProperties(schema, validData, validationFailureCollector)
            return@collectAndThrowExceptionOnValidationFailures validData
        }
    }

    @Throws(DataValidationException::class)
    fun validateEntryWithoutReferenceAndCardinalityIntegrity(
        schema: TypeSafeSchemaAccess,
        clazzModelDataEntry: TypeSafeClazzModel,
    ) {
        return collectAndThrowExceptionOnValidationFailures { validationFailureCollector ->
            val checks = ValidationChecks(validationFailureCollector)
            checks.checkIsKnownClazz(schema, clazzModelDataEntry.clazz)
            ClazzPropertyDataValidator.validateClazzPropertiesWithoutReferencesAndCardinalities(
                schema,
                clazzModelDataEntry,
                validationFailureCollector,
            )
        }
    }

    private fun validateClazzAndIdentifier(
        schema: TypeSafeSchemaAccess,
        clazzModelDataEntries: List<TypeSafeClazzModel>,
        dataValidationFailureCollector: DataValidationFailureCollector,
    ): Map<ClazzModelId, TypeSafeClazzModel> {
        val validData: MutableMap<ClazzModelId, TypeSafeClazzModel> = mutableMapOf()

        val allClazzModelIds: MutableSet<ClazzModelId> = mutableSetOf()
        clazzModelDataEntries.forEach { clazzDataEntry ->
            dataValidationFailureCollector.withSubCollector { clazzDataValidationFailureCollector ->
                val checks = ValidationChecks(clazzDataValidationFailureCollector)
                checks.checkIsKnownClazz(schema, clazzDataEntry.clazz)
                checks.checkIsNotDuplicateClazzModelId(
                    allClazzModelIds,
                    clazzDataEntry.clazz,
                    clazzDataEntry.clazzModelId,
                )

                allClazzModelIds.add(clazzDataEntry.clazzModelId)
                if (clazzDataValidationFailureCollector.isEmpty()) {
                    validData[clazzDataEntry.clazzModelId] = clazzDataEntry
                }
            }
        }
        return validData
    }

    private class ValidationChecks(private val validationFailureCollector: DataValidationFailureCollector) {
        private fun addValidationFailure(errorCode: DataCollectionErrorCode, vararg arguments: Any) {
            val message = errorCode.format(*arguments)
            validationFailureCollector.add(errorCode, message)
        }

        fun checkIsKnownClazz(schema: TypeSafeSchemaAccess, clazz: Clazz) {
            if (schema.clazzSchemaByClazz(clazz) == null) {
                addValidationFailure(DataCollectionErrorCode.UNKNOWN_CLAZZ, clazz)
            }
        }

        fun checkIsNotDuplicateClazzModelId(
            allClazzModelIds: Set<ClazzModelId>,
            clazz: Clazz,
            clazzModelId: ClazzModelId,
        ) {
            if (allClazzModelIds.contains(clazzModelId)) {
                addValidationFailure(DataCollectionErrorCode.DUPLICATE_CLAZZ_IDENTIFIER, clazzModelId.name, clazz)
            }
        }

        fun checkIsInstantiableClazz(schemaAccess: TypeSafeSchemaAccess, clazz: Clazz) {
            val clazzSchema = schemaAccess.clazzSchemaByClazz(clazz)
            if (clazzSchema == null) {
                addValidationFailure(DataCollectionErrorCode.UNKNOWN_CLAZZ, clazz)
                return
            }

            if (clazzSchema.clazzKindInformation.clazzKind == ClazzKind.ONLY_CONSTRUCTED_INSTANCE) {
                throw DataValidationException(
                    DataCollectionErrorCode.NON_INSTANTIABLE_CLAZZ.withFormattedMessage(
                        clazz.simpleName(),
                        clazzSchema.clazzKindInformation.clazzKindReasons.joinToString("\n") { it.message },
                    ),
                    clazzSchema.clazzKindInformation.clazzKindReasons,
                )
            }
        }
    }
}
