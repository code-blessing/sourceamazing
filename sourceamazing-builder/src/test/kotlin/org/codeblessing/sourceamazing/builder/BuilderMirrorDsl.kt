package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.typemirror.BuilderAnnotationMirror
import org.codeblessing.sourceamazing.builder.typemirror.BuilderMethodAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirror

object BuilderMirrorDsl {
    const val DEFAULT_PACKAGE_NAME = "org.codeblessing.sourceamazing.test.mock"

    @DslMarker
    annotation class BuilderDslMarker

    @BuilderDslMarker
    class BuilderDsl {
        private var builderClassMirror: ClassMirror = ClassMirror
            .interfaceMirror("TestBuilder")
            .setIsInterface()
            .withPackage(DEFAULT_PACKAGE_NAME)


        fun setBuilderIsClass() {
            builderClassMirror = builderClassMirror.setIsClass()
        }

        fun setBuilderIsAnnotation() {
            builderClassMirror = builderClassMirror.setIsAnnotation()
        }

        fun setBuilderIsEnum() {
            builderClassMirror = builderClassMirror.setIsEnum()
        }

        fun setBuilderIsObjectClass() {
            builderClassMirror = builderClassMirror.setIsObjectClass()
        }

        fun withAnnotationOnBuilder(annotation: AnnotationMirror) {
            builderClassMirror = builderClassMirror.withAnnotation(annotation)
        }


        fun withSuperClassMirror(superClassMirror: ClassMirror) {
            builderClassMirror = builderClassMirror.withSuperClass(superClassMirror)
        }


        private val builderFunctionMirrors: MutableList<FunctionMirror> = mutableListOf()

        fun builderMethod(addBuilderMethodAnnotation: Boolean = true, methodConfiguration: MethodDsl.() -> Unit): FunctionMirror {
            val methodDsl = MethodDsl("Method${builderFunctionMirrors.size}")
            if(addBuilderMethodAnnotation) {
                methodDsl.withAnnotationOnMethod(BuilderMethodAnnotationMirror())
            }
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            builderFunctionMirrors.add(methodMirror)
            return methodMirror
        }

        fun buildBuilderInterfaceMirror(addBuilderAnnotation: Boolean): ClassMirror {
            if(addBuilderAnnotation) {
                builderClassMirror = builderClassMirror.withAnnotation(BuilderAnnotationMirror())
            }
            builderFunctionMirrors.forEach { schemaMethod ->
                builderClassMirror = builderClassMirror.withMethod(schemaMethod)
            }
            return builderClassMirror
        }
    }

    @BuilderDslMarker
    class MethodDsl(methodName: String) {
        private var functionMirror: FunctionMirror = FunctionMirror.methodMirror(methodName)

        fun withMethodName(methodName: String) {
            functionMirror = functionMirror.withMethodName(methodName)
        }

        fun withReturnType(returnType: TypeMirror) {
            functionMirror = functionMirror.withReturnType(returnType)
        }

        fun withReturnType(returnType: ClassMirror, nullable: Boolean = false, vararg parameterAnnotations: AnnotationMirror) {
            functionMirror = functionMirror.withReturnType(
                returnClass = returnType,
                nullable = nullable,
                returnTypeAnnotations = parameterAnnotations
            )
        }

        fun withParameter(parameterName: String, parameterClassMirror: ClassMirror, nullable: Boolean = false, vararg parameterAnnotation: AnnotationMirror) {
            functionMirror = functionMirror.withParameter(
                parameterName = parameterName,
                parameterClass = parameterClassMirror,
                nullable = nullable,
                parameterAnnotation = parameterAnnotation
            )
        }

        /**
         * something like "myParameter: MyBuilder.() -> Unit"
         */
        fun withFunctionParameter(parameterName: String, parameterFunction: FunctionMirror, nullable: Boolean = false, vararg parameterAnnotation: AnnotationMirror) {
            functionMirror = functionMirror.withParameter(
                parameterName = parameterName,
                parameterClass = parameterFunction,
                nullable = nullable,
                parameterAnnotation = parameterAnnotation
            )
        }

        fun withAnnotationOnMethod(annotation: AnnotationMirror) {
            functionMirror = functionMirror.withAnnotation(annotation)
        }


        fun buildMethodMirror(): FunctionMirror {
            return functionMirror
        }
    }

    fun builder(addBuilderAnnotation: Boolean = true, configuration: BuilderDsl.() -> Unit): ClassMirror {
        val schemaDsl = BuilderDsl()
        configuration.invoke(schemaDsl)
        return schemaDsl.buildBuilderInterfaceMirror(addBuilderAnnotation)
    }
}