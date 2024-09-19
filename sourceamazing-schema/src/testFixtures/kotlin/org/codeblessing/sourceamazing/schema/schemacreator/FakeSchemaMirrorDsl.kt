package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKFunction
import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors.DEFAULT_PACKAGE_NAME
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import kotlin.reflect.KClass

object FakeSchemaMirrorDsl {
    @DslMarker
    annotation class SchemaDslMarker

    @SchemaDslMarker
    class SchemaDsl {
        private val schemaClassMirror: FakeKClass = FakeKClass
            .interfaceMirror("Schema")
            .setIsInterface()
            .withPackage(DEFAULT_PACKAGE_NAME)


        private val schemaConceptClassMirrors: MutableList<FakeKClass> = mutableListOf()

        fun setSchemaIsClass() {
            schemaClassMirror.setIsClass()
        }

        fun setSchemaIsAnnotation() {
            schemaClassMirror.setIsAnnotation()
        }

        fun setSchemaIsEnum() {
            schemaClassMirror.setIsEnum()
        }

        fun setSchemaIsObjectClass() {
            schemaClassMirror.setIsObjectClass()
        }

        fun withAnnotationOnSchema(annotation: Annotation) {
            schemaClassMirror.withAnnotation(annotation)
        }


        fun withSuperClassMirror(superClassMirror: KClass<*>) {
            schemaClassMirror.withSuperClass(superClassMirror)
        }


        fun concept(addConceptAnnotationWithAllFacets: Boolean = true, configuration: ConceptDsl.() -> Unit): FakeKClass {
            val conceptDsl = ConceptDsl("Concept${schemaConceptClassMirrors.size}")
            configuration.invoke(conceptDsl)
            val conceptClassMirror = conceptDsl.buildConceptMirror(addConceptAnnotationWithAllFacets)
            schemaConceptClassMirrors.add(conceptClassMirror)
            return conceptClassMirror
        }

        fun concept(conceptClassMirror: FakeKClass): FakeKClass {
            schemaConceptClassMirrors.add(conceptClassMirror)
            return conceptClassMirror
        }

        private val schemaFunctionMirrors: MutableList<FakeKFunction> = mutableListOf()

        fun method(methodConfiguration: MethodDsl.() -> Unit): FakeKFunction {
            val methodDsl = MethodDsl("Method${schemaFunctionMirrors.size}")
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            schemaFunctionMirrors.add(methodMirror)
            return methodMirror
        }

        fun buildSchemaMirror(addSchemaAnnotationWithAllConcepts: Boolean): FakeKClass {
            if(addSchemaAnnotationWithAllConcepts) {
                schemaClassMirror.withAnnotation(Schema(schemaConceptClassMirrors.toTypedArray()))
            }

            schemaFunctionMirrors.forEach { schemaMethod ->
                schemaClassMirror.withMethod(schemaMethod)
            }

            return schemaClassMirror
        }
    }

    @SchemaDslMarker
    class ConceptDsl(conceptClassName: String) {
        var conceptMirror: FakeKClass = FakeKClass
            .interfaceMirror(conceptClassName)
            .withPackage(DEFAULT_PACKAGE_NAME)
        private val conceptFacetClassMirrors: MutableList<FakeKClass> = mutableListOf()
        private val conceptFunctionMirrors: MutableList<FakeKFunction> = mutableListOf()

        fun setConceptIsClass() {
            conceptMirror = conceptMirror.setIsClass()
        }

        fun withAnnotationOnConcept(annotation: Annotation) {
            conceptMirror = conceptMirror.withAnnotation(annotation)
        }

        fun withConceptClassName(conceptClassName: String) {
            conceptMirror = conceptMirror.withClassName(conceptClassName)
        }

        fun withSuperClassMirror(superClassMirror: FakeKClass) {
            conceptMirror = conceptMirror.withSuperClass(superClassMirror)
        }

        fun facet(addFacetToConcept: Boolean = true, facetConfiguration: FacetDsl.() -> Unit): FakeKClass {
            val facetDsl = FacetDsl("Facet${conceptFacetClassMirrors.size}")
            facetConfiguration.invoke(facetDsl)
            val facetClassMirror = facetDsl.buildFacetMirror()
            if(addFacetToConcept) {
                conceptFacetClassMirrors.add(facetClassMirror)
            }
            return facetClassMirror
        }

        fun method(methodConfiguration: MethodDsl.() -> Unit): FakeKFunction {
            val methodDsl = MethodDsl("Method${conceptFunctionMirrors.size}")
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            conceptFunctionMirrors.add(methodMirror)
            return methodMirror
        }


        fun buildConceptMirror(addConceptAnnotationWithAllFacets: Boolean = true): FakeKClass {
            if(addConceptAnnotationWithAllFacets) {
                conceptMirror = conceptMirror.withAnnotation(Concept(facets = conceptFacetClassMirrors.toTypedArray()))
            }
            conceptFunctionMirrors.forEach { schemaMethod ->
                conceptMirror = conceptMirror.withMethod(schemaMethod)
            }
            return conceptMirror
        }
    }


    @SchemaDslMarker
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

        fun withParameter(parameterName: String, parameterClassMirror: FakeKClass, nullable: Boolean = false, vararg parameterAnnotations: Annotation) {
            functionMirror = functionMirror.withParameter(
                parameterName = parameterName,
                parameterClass = parameterClassMirror,
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

    @SchemaDslMarker
    class FacetDsl(facetClassName: String) {
        var facetMirror: FakeKClass = FakeKClass
            .interfaceMirror(facetClassName)
            .withPackage(DEFAULT_PACKAGE_NAME)

        fun setFacetIsNotInterface() {
            facetMirror = facetMirror.setIsClass()
        }

        fun withAnnotationOnFacet(annotation: Annotation) {
            facetMirror = facetMirror.withAnnotation(annotation)
        }

        fun withFacetClassName(conceptClassName: String) {
            facetMirror = facetMirror.withClassName(conceptClassName)
        }

        fun withSuperClassMirrorForFacet(superClassMirror: FakeKClass) {
            facetMirror = facetMirror.withSuperClass(superClassMirror)
        }

        fun buildFacetMirror(): FakeKClass {
            return facetMirror
        }

    }

    fun schema(addSchemaAnnotationWithAllConcepts: Boolean = true, configuration: SchemaDsl.() -> Unit): FakeKClass {
        val schemaDsl = SchemaDsl()
        configuration.invoke(schemaDsl)
        return schemaDsl.buildSchemaMirror(addSchemaAnnotationWithAllConcepts)
    }

    fun concept(addConceptAnnotationWithAllFacets: Boolean = true, configuration: ConceptDsl.() -> Unit): FakeKClass {
        val conceptBuilder = ConceptDsl("Concept")
        configuration.invoke(conceptBuilder)
        return conceptBuilder.buildConceptMirror(addConceptAnnotationWithAllFacets = addConceptAnnotationWithAllFacets)
    }

}