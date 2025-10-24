package org.codeblessing.sourceamazing.builder.interpretation

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import org.codeblessing.sourceamazing.builder.Alias
import org.codeblessing.sourceamazing.builder.MethodLocation
import org.codeblessing.sourceamazing.builder.alias.toAlias
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzModelIdAnnotationData
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzPropertyAnnotationBaseData
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzPropertyAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzPropertyReferenceAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.FixedClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.ReferenceClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.SetClazzPropertyReferenceAnnotationContent
import org.codeblessing.sourceamazing.builder.interpretation.clazzproperty.SetClazzPropertyValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.builder.utils.EnumUtil
import org.codeblessing.sourceamazing.schema.typesafeapi.ClassProperty
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzPropertySchema
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeSchemaAccess
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazzProperty
import org.codeblessing.sourceamazing.schema.utils.type.KTypeUtil
import org.codeblessing.sourceamazing.schema.utils.type.hasAnnotation
import org.codeblessing.sourceamazing.schema.utils.type.isEnum
import org.codeblessing.sourceamazing.schema.utils.type.receiverParameter
import org.codeblessing.sourceamazing.schema.utils.type.returnTypeOrNull
import org.codeblessing.sourceamazing.schema.utils.type.valueParameters

class BuilderMethodInterpreter(
    val schemaAccess: TypeSafeSchemaAccess,
    val builderClassInterpreter: BuilderClassInterpreter,
    val method: KFunction<*>,
) {
    val methodLocation = MethodLocation.create(method)

    fun isBuilderMethod(): Boolean {
        return method.hasAnnotation(BuilderMethod::class)
    }

    fun isDefaultMethod(): Boolean {
        return method.javaMethod?.isDefault ?: false
    }

    fun newClazzesAndExpectedClazzesFromSuperiorBuilder(): Map<Alias, Clazz> {
        val newClazzesFromMethod: Map<Alias, Clazz> = newClazzes()
        val expectedClazzesFromSuperiorMethod: Map<Alias, Clazz> =
            builderClassInterpreter.newClazzesFromSuperiorBuilderFilteredByExpectedAliases()
        return newClazzesFromMethod + expectedClazzesFromSuperiorMethod
    }

    fun getBuilderClassFromInjectBuilderParameter(): KClass<*>? {
        val methodParameter = method.valueParameters.lastOrNull() ?: return null
        if (!methodParameter.hasAnnotation<InjectBuilder>()) {
            return null
        }

        val injectionBuilderKType = methodParameter.type
        val receiverParameterType =
            requireNotNull(injectionBuilderKType.receiverParameter()) { "receiverParameterType must not be null" }

        return KTypeUtil.classFromType(KTypeUtil.kTypeFromProjection(receiverParameterType))
    }

    fun getBuilderClassFromReturnType(): KClass<*>? {
        val methodReturnType = method.returnTypeOrNull() ?: return null
        return KTypeUtil.classesInformationFromKType(methodReturnType).first().clazz
    }

    fun getClazzByAlias(clazzAlias: Alias): Clazz {
        val newClazzesByAlias: Map<Alias, Clazz> = newClazzes()

        return newClazzesByAlias[clazzAlias]
            ?: throw IllegalStateException("Can not find clazz name for alias '$clazzAlias' on method $method")
    }

    fun newClazzes(): Map<Alias, Clazz> {
        return newClazzesAsPairListIncludingDuplicates().associate { it }
    }

    fun newClazzAliasesIncludingDuplicates(): List<Alias> {
        return newClazzesAsPairListIncludingDuplicates().map { it.first }
    }

    fun newClazzAliases(): Set<Alias> {
        return newClazzes().keys
    }

    fun newClazzByAlias(alias: Alias): Clazz {
        return requireNotNull(newClazzes()[alias]) { "No clazz found for alias $alias in ${newClazzes()}." }
    }

    fun aliasesToSetClazzModelIdValueAliasesIncludingDuplicates(): List<Alias> {
        return method
            .valueParameters()
            .flatMap { parameter -> parameter.annotations.filterIsInstance<SetAsClazzModelId>() }
            .map { it.alias.toAlias() }
    }

    fun getClazzPropertyValueOrReferenceAnnotationContent(
        dataContext: DataContext? = null
    ): List<ClazzPropertyAnnotationContent> {
        return getClazzPropertyValueAnnotationContent(dataContext) +
            getClazzPropertyReferenceAnnotationContent(dataContext)
    }

    fun getClazzPropertyValueAnnotationContent(
        dataContext: DataContext? = null
    ): List<ClazzPropertyValueAnnotationContent> {
        return getFixedClazzPropertyValues() + getMethodParamAssignedClazzPropertyValues(dataContext)
    }

    fun getClazzPropertyReferenceAnnotationContent(
        dataContext: DataContext? = null
    ): List<ClazzPropertyReferenceAnnotationContent> {
        return getFixedClazzPropertyReferences(dataContext) + getMethodParamAssignedClazzPropertyReferences(dataContext)
    }

    fun getManualAssignedClazzModelIdAnnotationContent(
        dataContext: DataContext? = null
    ): List<ClazzModelIdAnnotationData> {
        return method.valueParameters.flatMap { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetAsClazzModelId>().map { annotation ->
                val clazzModelId = dataContext?.valueForMethodParameter(methodParameter)?.let { createClazzModelId(it) }
                ClazzModelIdAnnotationData(
                    methodLocation = builderMethodParameterLocation(methodParameter),
                    alias = annotation.alias.toAlias(),
                    annotation = annotation,
                    ignoreNullValue = isIgnoreNullValue(methodParameter),
                    type = builderMethodParameterType(methodParameter),
                    clazzModelId = clazzModelId,
                )
            }
        }
    }

    private fun newClazzesAsPairListIncludingDuplicates(): List<Pair<Alias, Clazz>> {
        return method.annotations.filterIsInstance<NewClazzModel>().map { Pair(it.alias.toAlias(), it.clazz.toClazz()) }
    }

    private fun getFixedClazzPropertyValues(): List<ClazzPropertyValueAnnotationContent> {
        val aliases = newClazzesAndExpectedClazzesFromSuperiorBuilder()
        val clazzPropertyValues: MutableList<ClazzPropertyValueAnnotationContent> = mutableListOf()

        method.annotations
            .filterIsInstance<SetFixedBooleanValue>()
            .map { annotation ->
                FixedClazzPropertyValueAnnotationContent(
                    base =
                        ClazzPropertyAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.alias.toAlias(),
                            classProperty = annotation.clazzProperty.toClazzProperty(),
                            clazzPropertyModification = annotation.modification,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                        ),
                    value = annotation.value,
                )
            }
            .forEach(clazzPropertyValues::add)

        method.annotations
            .filterIsInstance<SetFixedEnumValue>()
            .map { annotation ->
                val enumClazzAlias = annotation.alias.toAlias()
                val clazzProperty = annotation.clazzProperty.toClazzProperty()
                // If the clazzPropertySchema is not found, this will be caught by the validation as no alias found
                // exception.
                // Having a null-value for the enum will be validated later and not show up in a misleading
                // validation message.
                val clazzPropertySchema =
                    resolveClazzPropertySchema(aliases, enumClazzAlias, clazzProperty, schemaAccess)
                // This enum value is used for the validation AND the data retrieval!
                val enumValue = clazzPropertySchema?.let { enumValueByString(it, annotation.value) }

                FixedClazzPropertyValueAnnotationContent(
                    base =
                        ClazzPropertyAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = enumClazzAlias,
                            classProperty = clazzProperty,
                            clazzPropertyModification = annotation.modification,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                        ),
                    value =
                        enumValue
                            ?: annotation
                                .value, // if the enumValue was not found, provide the corrupt enum string so it fails
                )
            }
            .forEach(clazzPropertyValues::add)

        method.annotations
            .filterIsInstance<SetFixedIntValue>()
            .map { annotation ->
                FixedClazzPropertyValueAnnotationContent(
                    base =
                        ClazzPropertyAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.alias.toAlias(),
                            classProperty = annotation.clazzProperty.toClazzProperty(),
                            clazzPropertyModification = annotation.modification,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                        ),
                    value = annotation.value,
                )
            }
            .forEach(clazzPropertyValues::add)

        method.annotations
            .filterIsInstance<SetFixedStringValue>()
            .map { annotation ->
                FixedClazzPropertyValueAnnotationContent(
                    base =
                        ClazzPropertyAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.alias.toAlias(),
                            classProperty = annotation.clazzProperty.toClazzProperty(),
                            clazzPropertyModification = annotation.modification,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                        ),
                    value = annotation.value,
                )
            }
            .forEach(clazzPropertyValues::add)
        return clazzPropertyValues
    }

    private fun getFixedClazzPropertyReferences(
        dataContext: DataContext? = null
    ): List<ClazzPropertyReferenceAnnotationContent> {
        val clazzPropertyReferences: MutableList<ClazzPropertyReferenceAnnotationContent> = mutableListOf()

        method.annotations
            .filterIsInstance<SetClazzModelOfAlias>()
            .map { annotation ->
                val referencedAlias = annotation.referencedAlias.toAlias()
                val referenceClazzModelId = dataContext?.clazzModelIdByAlias(referencedAlias)

                ReferenceClazzPropertyValueAnnotationContent(
                    base =
                        ClazzPropertyAnnotationBaseData(
                            methodLocation = methodLocation,
                            alias = annotation.alias.toAlias(),
                            classProperty = annotation.clazzProperty.toClazzProperty(),
                            clazzPropertyModification = annotation.clazzPropertyModification,
                            annotation = annotation,
                            ignoreNullValue = false,
                            type = null,
                        ),
                    referencedAlias = referencedAlias,
                    value = referenceClazzModelId,
                )
            }
            .forEach(clazzPropertyReferences::add)
        return clazzPropertyReferences
    }

    private fun getMethodParamAssignedClazzPropertyValues(
        dataContext: DataContext? = null
    ): List<ClazzPropertyValueAnnotationContent> {
        return method.valueParameters.flatMap { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetAsValue>().map { annotation ->
                val value = dataContext?.valueForMethodParameter(methodParameter)
                SetClazzPropertyValueAnnotationContent(
                    base =
                        ClazzPropertyAnnotationBaseData(
                            methodLocation = builderMethodParameterLocation(methodParameter),
                            alias = annotation.alias.toAlias(),
                            classProperty = annotation.clazzProperty.toClazzProperty(),
                            clazzPropertyModification = annotation.modification,
                            annotation = annotation,
                            ignoreNullValue = isIgnoreNullValue(methodParameter),
                            type = builderMethodParameterType(methodParameter),
                        ),
                    value = value,
                )
            }
        }
    }

    private fun getMethodParamAssignedClazzPropertyReferences(
        dataContext: DataContext? = null
    ): List<ClazzPropertyReferenceAnnotationContent> {
        return method.valueParameters.flatMap { methodParameter ->
            methodParameter.annotations.filterIsInstance<SetClazzModelOfId>().map { annotation ->
                val value = dataContext?.valueForMethodParameter(methodParameter)
                SetClazzPropertyReferenceAnnotationContent(
                    base =
                        ClazzPropertyAnnotationBaseData(
                            methodLocation = builderMethodParameterLocation(methodParameter),
                            alias = annotation.alias.toAlias(),
                            classProperty = annotation.clazzProperty.toClazzProperty(),
                            clazzPropertyModification = annotation.modification,
                            annotation = annotation,
                            ignoreNullValue = isIgnoreNullValue(methodParameter),
                            type = builderMethodParameterType(methodParameter),
                        ),
                    value = value,
                )
            }
        }
    }

    private fun builderMethodParameterLocation(methodParameter: KParameter): MethodLocation {
        return methodLocation.extendWithMethodParam(methodParameter)
    }

    private fun builderMethodParameterType(methodParameter: KParameter): KType {
        return methodParameter.type
    }

    private fun isIgnoreNullValue(methodParameter: KParameter): Boolean {
        return methodParameter.hasAnnotation<IgnoreNullValue>()
    }

    private fun resolveClazzPropertySchema(
        aliases: Map<Alias, Clazz>,
        clazzAlias: Alias,
        classProperty: ClassProperty,
        schemaAccess: TypeSafeSchemaAccess,
    ): TypeSafeClazzPropertySchema? {
        val clazz = aliases[clazzAlias] ?: return null
        return schemaAccess.clazzSchemaByClazz(clazz)?.clazzPropertyByName(classProperty)
    }

    private fun enumValueByString(clazzPropertySchema: TypeSafeClazzPropertySchema, enumValueString: String): Enum<*>? {
        return if (clazzPropertySchema.clazzPropertyClazz.clazz.isEnum) {
            return EnumUtil.fromStringToEnum(enumValueString, clazzPropertySchema.clazzPropertyClazz.clazz)
        } else {
            null
        }
    }

    private fun createClazzModelId(value: Any): ClazzModelId {
        return when (value) {
            is ClazzModelId -> value
            else -> ClazzModelId.of(value)
        }
    }

    fun aliasRedeclarations(): Map<Alias, Alias> {
        return method.annotations.filterIsInstance<RedeclareAliasForNestedBuilder>().associate { annotation ->
            Pair(Alias.of(annotation.alias), Alias.of(annotation.newAlias))
        }
    }
}
