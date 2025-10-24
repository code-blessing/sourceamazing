package org.codeblessing.sourceamazing.schema.clazzgraph

import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.datacollection.validation.ClazzModelValidator
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess

object ClazzResolver {

    @Throws(DataValidationException::class)
    fun validateAndResolveClasses(
        schema: TypeSafeSchemaAccess,
        clazzModelDataEntries: List<TypeSafeClazzModel>,
    ): ClazzInstanceGraph {
        val validatedDataEntries = ClazzModelValidator.validateEntries(schema, clazzModelDataEntries)
        val clazzNodeMap: Map<ClazzModelId, MutableClazzInstance> = createClazzNodeMap(schema, validatedDataEntries)
        return ClazzInstanceGraph(clazzNodeMap)
    }

    private fun createClazzNodeMap(
        schema: TypeSafeSchemaAccess,
        clazzModelDataEntries: Map<ClazzModelId, TypeSafeClazzModel>,
    ): Map<ClazzModelId, MutableClazzInstance> {
        val clazzNodeMap: MutableMap<ClazzModelId, MutableClazzInstance> = mutableMapOf()

        // 1. Phase: Create entry without clazzProperty values
        clazzModelDataEntries.forEach { (clazzModelId, clazzData) ->
            clazzNodeMap[clazzModelId] =
                MutableClazzInstance(
                    sequenceNumber = clazzData.sequenceNumber,
                    clazz = clazzData.clazz,
                    clazzModelId = clazzData.clazzModelId,
                )
        }

        // 2. Phase: Fill in clazzProperty values and connect with/resolve other referenced clazz
        // instances
        //          (resolve the clazz identifier to the real clazz node)
        clazzNodeMap.forEach { (clazzModelId, clazzNode) ->
            val clazzData = requireNotNull(clazzModelDataEntries[clazzModelId]) { "Could not resolve $clazzModelId." }
            val clazzSchema =
                requireNotNull(schema.clazzSchemaByClazz(clazzNode.clazz)) { "Could not resolve ${clazzNode.clazz}." }
            clazzSchema.clazzProperties.forEach { clazzPropertySchema ->
                clazzNode.clazzPropertyValues[clazzPropertySchema.classProperty] =
                    transformClazzPropertyValues(clazzPropertySchema, clazzData, clazzNodeMap)
            }
        }
        // 3. Phase: Check for unresolvable circular dependencies
        CircularDependencyValidator.checkForUnresolvableCircularDependencies(clazzNodeMap)

        return clazzNodeMap
    }

    private fun transformClazzPropertyValues(
        clazzPropertySchema: TypeSafeClazzPropertySchema,
        clazzModelData: TypeSafeClazzModel,
        clazzNodeMap: MutableMap<ClazzModelId, MutableClazzInstance>,
    ): List<Any> {
        return clazzModelData.getClazzPropertyValues(clazzPropertySchema.classProperty).map { value ->
            transformClazzPropertyValue(value, clazzModelData, clazzNodeMap)
        }
    }

    private fun transformClazzPropertyValue(
        clazzPropertyValue: Any,
        clazzModelData: TypeSafeClazzModel,
        clazzNodeMap: MutableMap<ClazzModelId, MutableClazzInstance>,
    ): Any {
        if (clazzPropertyValue is ClazzModelId) {
            return clazzNodeMap[clazzPropertyValue]
                ?: throw IllegalStateException(
                    "Could not resolve reference to $clazzPropertyValue from $${clazzModelData.clazzModelId}. "
                )
        }
        return clazzPropertyValue
    }
}
