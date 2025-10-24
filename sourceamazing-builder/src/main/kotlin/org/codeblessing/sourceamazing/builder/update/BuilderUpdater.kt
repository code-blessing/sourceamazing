package org.codeblessing.sourceamazing.builder.update

import kotlin.reflect.KFunction
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.api.annotations.ClazzPropertyModification
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullValue
import org.codeblessing.sourceamazing.builder.documentation.TypesAsTextFunctions.annotationText
import org.codeblessing.sourceamazing.builder.interpretation.BuilderCollectionHelper
import org.codeblessing.sourceamazing.builder.interpretation.BuilderMethodInterpreter
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.FixedClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ReferenceClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.SetClazzPropertyReferenceAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.SetClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.datacollection.TypeSafeClazzModel
import org.codeblessing.sourceamazing.schema.typesafeapi.randomClazzModelId

object BuilderUpdater {

    fun updateClazzModelCollector(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        addNewClazzDataWithDedicatedClazzModelId(builderMethodInterpreter, builderInterpreterDataCollector)
        addNewClazzDataWithRandomClazzModelId(builderMethodInterpreter, builderInterpreterDataCollector)
        updateClazzPropertyValues(builderMethodInterpreter, builderInterpreterDataCollector)
    }

    private fun addNewClazzDataWithDedicatedClazzModelId(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        val method: KFunction<*> = builderMethodInterpreter.method
        val dataContext = builderInterpreterDataCollector.getDataContext()

        builderMethodInterpreter.getManualAssignedClazzModelIdAnnotationContent(dataContext).forEach {
            clazzModelIdAnnotationData ->
            val clazzAlias = clazzModelIdAnnotationData.alias
            val clazz = builderMethodInterpreter.getClazzByAlias(clazzAlias)
            val clazzModelId =
                clazzModelIdAnnotationData.clazzModelId
                    ?: throw IllegalArgumentException(
                        "Can not pass null value as clazz identifier argument on method $method"
                    )
            builderInterpreterDataCollector.newClazzData(clazzAlias, clazz, clazzModelId)
        }
    }

    private fun addNewClazzDataWithRandomClazzModelId(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        val allNewClazzAliases = builderMethodInterpreter.newClazzAliases()
        val allClazzesWithDedicatedClazzModelIds =
            builderMethodInterpreter.aliasesToSetClazzModelIdValueAliasesIncludingDuplicates()
        val aliasesToSetRandomClazzModelId = allNewClazzAliases - allClazzesWithDedicatedClazzModelIds
        aliasesToSetRandomClazzModelId.forEach { clazzAlias ->
            val clazz = builderMethodInterpreter.getClazzByAlias(clazzAlias)
            val clazzModelId = clazz.randomClazzModelId()
            builderInterpreterDataCollector.newClazzData(clazzAlias, clazz, clazzModelId)
        }
    }

    private fun updateClazzPropertyValues(
        builderMethodInterpreter: BuilderMethodInterpreter,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        val dataContext = builderInterpreterDataCollector.getDataContext()
        builderMethodInterpreter.getClazzPropertyValueOrReferenceAnnotationContent(dataContext).forEach {
            clazzPropertyAnnotationContent ->
            val value: Any =
                clazzPropertyAnnotationContent.value
                    ?: if (!clazzPropertyAnnotationContent.base.ignoreNullValue) {
                        throw IllegalArgumentException(
                            "Can not pass null values at '${clazzPropertyAnnotationContent.base.methodLocation}' " +
                                "on method ${builderMethodInterpreter.method}. If this is wanted, use the annotation '${IgnoreNullValue::class.annotationText()}'."
                        )
                    } else {
                        return@forEach // skip null values silently
                    }

            val isReferences =
                when (clazzPropertyAnnotationContent) {
                    is ReferenceClazzPropertyValueAnnotationContent -> true
                    is SetClazzPropertyReferenceAnnotationContent -> true
                    is FixedClazzPropertyValueAnnotationContent -> false
                    is SetClazzPropertyValueAnnotationContent -> false
                }
            updateClazzData(
                clazzAlias = clazzPropertyAnnotationContent.base.alias,
                classProperty = clazzPropertyAnnotationContent.base.classProperty,
                value = value,
                isReferences = isReferences,
                clazzPropertyModification = clazzPropertyAnnotationContent.base.clazzPropertyModification,
                builderInterpreterDataCollector = builderInterpreterDataCollector,
            )
        }
    }

    private fun updateClazzData(
        clazzAlias: Alias,
        classProperty: ClassProperty,
        value: Any,
        isReferences: Boolean,
        clazzPropertyModification: ClazzPropertyModification,
        builderInterpreterDataCollector: BuilderMethodInterpreterDataCollector,
    ) {
        val clazzModelId: ClazzModelId = builderInterpreterDataCollector.clazzModelIdByAlias(clazzAlias)
        val clazzData = builderInterpreterDataCollector.existingClazzData(clazzModelId)
        val clazzPropertyValues = BuilderCollectionHelper.clazzPropertyValueListFromClazzPropertyValue(value)
        when (clazzPropertyModification) {
            ClazzPropertyModification.ADD -> addValues(classProperty, clazzPropertyValues, isReferences, clazzData)
            ClazzPropertyModification.REPLACE ->
                replaceValues(classProperty, clazzPropertyValues, isReferences, clazzData)
        }
        builderInterpreterDataCollector.validateAfterUpdate(clazzData)
    }

    private fun addValues(
        classProperty: ClassProperty,
        clazzPropertyValues: List<Any>,
        isReferences: Boolean,
        clazzData: TypeSafeClazzModel,
    ) {
        if (isReferences) {
            clazzData.addClazzReferences(classProperty, clazzPropertyValues)
        } else {
            clazzData.addValues(classProperty, clazzPropertyValues)
        }
    }

    private fun replaceValues(
        classProperty: ClassProperty,
        clazzPropertyValues: List<Any>,
        isReferences: Boolean,
        clazzData: TypeSafeClazzModel,
    ) {
        if (isReferences) {
            clazzData.replaceWithClazzReferences(classProperty, clazzPropertyValues)
        } else {
            clazzData.replaceWithValues(classProperty, clazzPropertyValues)
        }
    }
}
