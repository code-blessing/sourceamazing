package org.codeblessing.sourceamazing.schema.datacollection.validation

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzSchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.utils.type.enumValues
import org.codeblessing.sourceamazing.schema.utils.type.isEnum

object ClazzPropertyDataValidator {

    fun validateClazzProperties(
        schema: TypeSafeSchemaAccess,
        clazzModelDataMap: Map<ClazzModelId, TypeSafeClazzModel>,
        validationFailureCollector: DataValidationFailureCollector,
    ) {

        forEachClazz(schema, clazzModelDataMap) { clazzSchema, clazzDataEntry ->
            validationFailureCollector.withSubCollector { clazzValidationFailureCollector ->
                val checks = ValidationChecks(clazzValidationFailureCollector)
                checks.checkNoObsoletClazzProperties(clazzSchema, clazzDataEntry)
                checks.checkIsValidClazzPropertyTypes(clazzSchema, clazzDataEntry)

                // if we have wrong clazzProperties and wrong types, we early return and
                // avoid validation errors that are based on wrong types, etc.
                if (clazzValidationFailureCollector.isEmpty()) {
                    checks.checkCorrectClazzPropertyCardinality(clazzSchema, clazzDataEntry)
                    checks.checkNoMissingReferencedClazzes(clazzSchema, clazzDataEntry, clazzModelDataMap)
                    checks.checkNoWrongReferencedClazzes(clazzSchema, clazzDataEntry, clazzModelDataMap)
                }
            }
        }
    }

    fun validateClazzPropertiesWithoutReferencesAndCardinalities(
        schema: TypeSafeSchemaAccess,
        clazzModelDataEntry: TypeSafeClazzModel,
        validationFailureCollector: DataValidationFailureCollector,
    ) {
        val clazzSchema = schema.clazzSchemaByName(clazzModelDataEntry.clazz)
        val checks = ValidationChecks(validationFailureCollector)
        checks.checkNoObsoletClazzProperties(clazzSchema, clazzModelDataEntry)
        checks.checkIsValidClazzPropertyTypes(clazzSchema, clazzModelDataEntry)
    }

    private class ValidationChecks(private val validationFailureCollector: DataValidationFailureCollector) {
        private fun addValidationFailure(errorCode: DataCollectionErrorCode, vararg arguments: Any) {
            val message = errorCode.format(*arguments)
            validationFailureCollector.add(errorCode, message)
        }

        fun checkNoObsoletClazzProperties(clazzSchema: TypeSafeClazzSchema, clazzModelDataEntry: TypeSafeClazzModel) {
            // iterate through all entry clazzProperty values to find obsolet ones
            clazzModelDataEntry.getClassPropertyNames().forEach { clazzProperty ->
                checkNoObsoleteClazzProperty(clazzSchema, clazzModelDataEntry, clazzProperty)
            }
        }

        private fun checkNoObsoleteClazzProperty(
            clazzSchema: TypeSafeClazzSchema,
            clazzModelDataEntry: TypeSafeClazzModel,
            classProperty: ClassProperty,
        ) {
            if (clazzSchema.clazzPropertyByName(classProperty) == null) {
                addValidationFailure(
                    DataCollectionErrorCode.UNKNOWN_CLAZZ_PROPERTY,
                    classProperty,
                    clazzModelDataEntry.clazzModelId.name,
                    clazzModelDataEntry.clazz,
                    clazzSchema.classProperties,
                    clazzModelDataEntry.describe(),
                )
            }
        }

        fun checkIsValidClazzPropertyTypes(clazzSchema: TypeSafeClazzSchema, clazzModelDataEntry: TypeSafeClazzModel) {
            forEachClazzPropertyValue(clazzSchema, clazzModelDataEntry) { clazzPropertySchema, clazzPropertyValue ->
                checkClazzPropertyDataType(clazzModelDataEntry, clazzPropertySchema, clazzPropertyValue)
            }
        }

        fun checkNoWrongReferencedClazzes(
            clazzSchema: TypeSafeClazzSchema,
            clazzModelDataEntry: TypeSafeClazzModel,
            clazzModelDataMap: Map<ClazzModelId, TypeSafeClazzModel>,
        ) {
            forEachClazzPropertyValue(clazzSchema, clazzModelDataEntry) { clazzPropertySchema, clazzPropertyValue ->
                if (clazzPropertyValue is ClazzModelId) {
                    val referencedClazzModelId = clazzPropertyValue
                    checkIsReferencedClazzPossible(
                        clazzModelDataMap,
                        clazzModelDataEntry,
                        clazzPropertySchema,
                        referencedClazzModelId,
                    )
                }
            }
        }

        private fun checkIsReferencedClazzPossible(
            clazzModelDataMap: Map<ClazzModelId, TypeSafeClazzModel>,
            clazzModelDataEntry: TypeSafeClazzModel,
            clazzPropertySchema: TypeSafeClazzPropertySchema,
            referencedClazzModelId: ClazzModelId,
        ) {
            val referencedClazz = clazzModelDataMap[referencedClazzModelId]
            // case that referencedClazz == null is handled with another check
            if (referencedClazz != null && !clazzPropertySchema.isCompatibleClazzClass(referencedClazz.clazz.clazz)) {
                addValidationFailure(
                    DataCollectionErrorCode.WRONG_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
                    clazzPropertySchema.classProperty,
                    clazzModelDataEntry.clazzModelId.name,
                    clazzModelDataEntry.clazz,
                    referencedClazz.clazz,
                    clazzModelDataEntry.describe(),
                )
            }
        }

        fun checkNoMissingReferencedClazzes(
            clazzSchema: TypeSafeClazzSchema,
            clazzModelDataEntry: TypeSafeClazzModel,
            clazzModelDataMap: Map<ClazzModelId, TypeSafeClazzModel>,
        ) {
            forEachClazzPropertyValue(clazzSchema, clazzModelDataEntry) { clazzPropertySchema, clazzPropertyValue ->
                if (clazzPropertyValue is ClazzModelId) {
                    val referencedClazzModelId = clazzPropertyValue
                    checkIsReferencedClazzNotMissing(
                        clazzModelDataMap,
                        clazzModelDataEntry,
                        clazzPropertySchema,
                        referencedClazzModelId,
                    )
                }
            }
        }

        private fun checkIsReferencedClazzNotMissing(
            clazzModelDataMap: Map<ClazzModelId, TypeSafeClazzModel>,
            clazzModelDataEntry: TypeSafeClazzModel,
            clazzPropertySchema: TypeSafeClazzPropertySchema,
            referencedClazzModelId: ClazzModelId,
        ) {
            val referencedClazz = clazzModelDataMap[referencedClazzModelId]
            if (referencedClazz == null) {
                addValidationFailure(
                    DataCollectionErrorCode.MISSING_REFERENCED_CLAZZ_CLAZZ_PROPERTY_VALUE,
                    clazzPropertySchema.classProperty,
                    clazzModelDataEntry.clazzModelId.name,
                    clazzModelDataEntry.clazz,
                    referencedClazzModelId,
                    clazzModelDataEntry.describe(),
                )
            }
        }

        private fun checkClazzPropertyDataType(
            clazzModelDataEntry: TypeSafeClazzModel,
            clazzPropertySchema: TypeSafeClazzPropertySchema,
            clazzPropertyValue: Any,
        ) {
            val actualClass = clazzPropertyValue::class

            if (actualClass == ClazzModelId::class) {
                return // early return
            }

            val isValidType = clazzPropertySchema.isCompatibleClazzPropertyValue(clazzPropertyValue)
            if (!isValidType) {
                if (clazzPropertySchema.clazzPropertyClazz.clazz.isEnum) {
                    addValidationFailure(
                        DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_ENUM_TYPE,
                        clazzPropertySchema.classProperty,
                        clazzModelDataEntry.clazzModelId.name,
                        clazzModelDataEntry.clazz,
                        clazzPropertySchema.clazzPropertyClazz.clazz.enumValues,
                        clazzPropertyValue,
                        actualClass.longText(),
                        clazzModelDataEntry.describe(),
                    )
                } else {
                    addValidationFailure(
                        DataCollectionErrorCode.WRONG_CLAZZ_PROPERTY_TYPE,
                        clazzPropertySchema.classProperty,
                        clazzModelDataEntry.clazzModelId.name,
                        clazzModelDataEntry.clazz,
                        clazzPropertySchema.clazzPropertyClazz,
                        actualClass.longText(),
                        clazzPropertyValue,
                        clazzModelDataEntry.describe(),
                    )
                }
            }
        }

        fun checkCorrectClazzPropertyCardinality(
            clazzSchema: TypeSafeClazzSchema,
            clazzModelDataEntry: TypeSafeClazzModel,
        ) {
            forEachClazzProperty(clazzSchema) { clazzPropertySchema ->
                checkMinimumOccurrences(clazzPropertySchema, clazzModelDataEntry)
            }
            forEachClazzProperty(clazzSchema) { clazzPropertySchema ->
                checkMaximumOccurrences(clazzPropertySchema, clazzModelDataEntry)
            }
        }

        private fun checkMaximumOccurrences(
            clazzPropertySchema: TypeSafeClazzPropertySchema,
            clazzModelDataEntry: TypeSafeClazzModel,
        ) {
            val numberOfEntries = clazzModelDataEntry.getClazzPropertyValues(clazzPropertySchema.classProperty).size
            if (!clazzPropertySchema.isCompatibleMaxCardinality(numberOfEntries)) {
                addValidationFailure(
                    DataCollectionErrorCode.MAXIMUM_CARDINALITY_ERROR,
                    clazzPropertySchema.classProperty,
                    clazzModelDataEntry.clazzModelId.name,
                    clazzModelDataEntry.clazz,
                    clazzPropertySchema.maximumOccurrences,
                    numberOfEntries,
                    clazzModelDataEntry.describe(),
                )
            }
        }

        private fun checkMinimumOccurrences(
            clazzPropertySchema: TypeSafeClazzPropertySchema,
            clazzModelDataEntry: TypeSafeClazzModel,
        ) {
            val numberOfEntries = clazzModelDataEntry.getClazzPropertyValues(clazzPropertySchema.classProperty).size
            if (!clazzPropertySchema.isCompatibleMinCardinality(numberOfEntries)) {
                addValidationFailure(
                    DataCollectionErrorCode.MINIMUM_CARDINALITY_ERROR,
                    clazzPropertySchema.classProperty,
                    clazzModelDataEntry.clazzModelId.name,
                    clazzModelDataEntry.clazz,
                    clazzPropertySchema.minimumOccurrences,
                    numberOfEntries,
                    clazzModelDataEntry.describe(),
                )
            }
        }
    }

    private fun forEachClazz(
        schema: TypeSafeSchemaAccess,
        clazzModelDataMap: Map<ClazzModelId, TypeSafeClazzModel>,
        block: (TypeSafeClazzSchema, TypeSafeClazzModel) -> Unit,
    ) {
        clazzModelDataMap.values.forEach { clazzDataEntry ->
            val clazz = clazzDataEntry.clazz
            val clazzSchema = schema.clazzSchemaByName(clazz)
            block(clazzSchema, clazzDataEntry)
        }
    }

    private fun TypeSafeSchemaAccess.clazzSchemaByName(clazz: Clazz): TypeSafeClazzSchema {
        return requireNotNull(this.clazzSchemaByClazz(clazz)) { "Clazz '${clazz}' does not exist." }
    }

    private fun forEachClazzProperty(clazzSchema: TypeSafeClazzSchema, block: (TypeSafeClazzPropertySchema) -> Unit) {
        clazzSchema.clazzProperties.forEach { clazzPropertySchema -> block(clazzPropertySchema) }
    }

    private fun forEachClazzPropertyValue(
        clazzSchema: TypeSafeClazzSchema,
        clazzModelDataEntry: TypeSafeClazzModel,
        block: (TypeSafeClazzPropertySchema, Any) -> Unit,
    ) {
        clazzSchema.clazzProperties.forEach { clazzPropertySchema ->
            if (clazzModelDataEntry.hasClazzProperty(clazzPropertySchema.classProperty)) {
                val clazzPropertyValues = clazzModelDataEntry.getClazzPropertyValues(clazzPropertySchema.classProperty)
                clazzPropertyValues.forEach { clazzPropertyValue -> block(clazzPropertySchema, clazzPropertyValue) }
            }
        }
    }

    private fun KClass<*>.longText(): String {
        return java.name
    }
}
