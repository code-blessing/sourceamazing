package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKFunction

object FakeBuilderMirrorDsl {
    const val DEFAULT_PACKAGE_NAME = "org.codeblessing.sourceamazing.test.mock"

    @DslMarker
    annotation class BuilderDslMarker

    @BuilderDslMarker
    class BuilderDsl {
        private val builderClassMirror: FakeKClass = FakeKClass
            .interfaceMirror("TestBuilder")
            .setIsInterface()
            .withPackage(DEFAULT_PACKAGE_NAME)


        fun setBuilderIsClass() {
            builderClassMirror.setIsClass()
        }

        fun setBuilderIsAnnotation() {
            builderClassMirror.setIsAnnotation()
        }

        fun setBuilderIsEnum() {
            builderClassMirror.setIsEnum()
        }

        fun setBuilderIsObjectClass() {
            builderClassMirror.setIsObjectClass()
        }

        fun withAnnotationOnBuilder(annotation: Annotation) {
            builderClassMirror.withAnnotation(annotation)
        }


        fun withSuperClassMirror(superClassMirror: FakeKClass) {
            builderClassMirror.withSuperClass(superClassMirror)
        }


        private val builderFunctionMirrors: MutableList<FakeKFunction> = mutableListOf()

        fun builderMethod(addBuilderMethodAnnotation: Boolean = true, methodConfiguration: MethodDsl.() -> Unit): FakeKFunction {
            val methodDsl = MethodDsl("Method${builderFunctionMirrors.size}")
            if(addBuilderMethodAnnotation) {
                methodDsl.withAnnotationOnMethod(BuilderMethod())
            }
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            builderFunctionMirrors.add(methodMirror)
            return methodMirror
        }

        fun buildBuilderInterfaceMirror(addBuilderAnnotation: Boolean): FakeKClass {
            if(addBuilderAnnotation) {
                builderClassMirror.withAnnotation(Builder())
            }
            builderFunctionMirrors.forEach { schemaMethod ->
                builderClassMirror.withMethod(schemaMethod)
            }
            return builderClassMirror
        }
    }

    @BuilderDslMarker
    class MethodDsl(methodName: String) {
        private var functionMirror: FakeKFunction = FakeKFunction.methodMirror(methodName)

        fun withMethodName(methodName: String) {
            functionMirror = functionMirror.withMethodName(methodName)
        }

        fun withReturnType(returnType: FakeKClass, nullable: Boolean = false, vararg parameterAnnotations: Annotation) {
            functionMirror = functionMirror.withReturnType(
                returnClass = returnType,
                nullable = nullable,
                returnTypeAnnotations = parameterAnnotations
            )
        }

        fun withParameter(parameterName: String, parameterClassMirror: FakeKClass, nullable: Boolean = false, vararg parameterAnnotation: Annotation) {
            functionMirror = functionMirror.withParameter(
                parameterName = parameterName,
                parameterClass = parameterClassMirror,
                nullable = nullable,
                parameterAnnotations = parameterAnnotation
            )
        }

        /**
         * something like "myParameter: MyBuilder.() -> Unit"
         */
        fun withFunctionParameter(parameterName: String, parameterFunction: FakeKFunction, nullable: Boolean = false, vararg parameterAnnotations: Annotation) {
            functionMirror = functionMirror.withParameter(
                parameterName = parameterName,
                parameterFunction = parameterFunction,
                nullable = nullable,
                parameterAnnotations = parameterAnnotations
            )
        }

        fun withAnnotationOnMethod(annotation: Annotation) {
            functionMirror = functionMirror.withAnnotation(annotation)
        }


        fun buildMethodMirror(): FakeKFunction {
            return functionMirror
        }
    }

    fun builder(addBuilderAnnotation: Boolean = true, configuration: BuilderDsl.() -> Unit): FakeKClass {
        val schemaDsl = BuilderDsl()
        configuration.invoke(schemaDsl)
        return schemaDsl.buildBuilderInterfaceMirror(addBuilderAnnotation)
    }
}