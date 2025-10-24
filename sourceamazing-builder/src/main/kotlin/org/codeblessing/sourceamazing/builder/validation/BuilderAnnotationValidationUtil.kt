package org.codeblessing.sourceamazing.builder.validation

import kotlin.reflect.KAnnotatedElement
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.schema.utils.type.hasAnnotation

object BuilderAnnotationValidationUtil {

    private val builderAnnotations =
        listOf(
            Builder::class,
            BuilderMethod::class,
            ExpectedClazzModelFromSuperiorBuilder::class,
            IgnoreNullValue::class,
            InjectBuilder::class,
            NewClazzModel::class,
            SetClazzModelOfAlias::class,
            SetAsClazzModelId::class,
            SetAsValue::class,
            SetFixedBooleanValue::class,
            SetFixedEnumValue::class,
            SetFixedIntValue::class,
            SetFixedStringValue::class,
        )

    fun hasBuilderAnnotation(element: KAnnotatedElement): Boolean {
        for (builderAnnotation in builderAnnotations) {
            if (element.hasAnnotation(builderAnnotation)) {
                return true
            }
        }
        return false
    }
}
