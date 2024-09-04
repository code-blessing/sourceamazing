package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors.DEFAULT_PACKAGE_NAME
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeFunctionMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror

object FakeSchemaMirrorDsl {
    @DslMarker
    annotation class SchemaDslMarker

    @SchemaDslMarker
    class SchemaDsl {
        private var schemaClassMirror: FakeClassMirror = FakeClassMirror
            .interfaceMirror("Schema")
            .setIsInterface()
            .withPackage(DEFAULT_PACKAGE_NAME)


        private val schemaConceptClassMirrors: MutableList<FakeClassMirror> = mutableListOf()

        fun setSchemaIsClass() {
            schemaClassMirror = schemaClassMirror.setIsClass()
        }

        fun setSchemaIsAnnotation() {
            schemaClassMirror = schemaClassMirror.setIsAnnotation()
        }

        fun setSchemaIsEnum() {
            schemaClassMirror = schemaClassMirror.setIsEnum()
        }

        fun setSchemaIsObjectClass() {
            schemaClassMirror = schemaClassMirror.setIsObjectClass()
        }

        fun withAnnotationOnSchema(annotation: AnnotationMirror) {
            schemaClassMirror = schemaClassMirror.withAnnotation(annotation)
        }


        fun withSuperClassMirror(superClassMirror: FakeClassMirror) {
            schemaClassMirror = schemaClassMirror.withSuperClass(superClassMirror)
        }


        fun concept(addConceptAnnotationWithAllFacets: Boolean = true, configuration: ConceptDsl.() -> Unit): FakeClassMirror {
            val conceptDsl = ConceptDsl("Concept${schemaConceptClassMirrors.size}")
            configuration.invoke(conceptDsl)
            val conceptClassMirror = conceptDsl.buildConceptMirror(addConceptAnnotationWithAllFacets)
            schemaConceptClassMirrors.add(conceptClassMirror)
            return conceptClassMirror
        }

        fun concept(conceptClassMirror: FakeClassMirror): FakeClassMirror {
            schemaConceptClassMirrors.add(conceptClassMirror)
            return conceptClassMirror
        }

        private val schemaFunctionMirrors: MutableList<FakeFunctionMirror> = mutableListOf()

        fun method(methodConfiguration: MethodDsl.() -> Unit): FakeFunctionMirror {
            val methodDsl = MethodDsl("Method${schemaFunctionMirrors.size}")
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            schemaFunctionMirrors.add(methodMirror)
            return methodMirror
        }

        fun buildSchemaMirror(addSchemaAnnotationWithAllConcepts: Boolean): FakeClassMirror {
            if(addSchemaAnnotationWithAllConcepts) {
                schemaClassMirror = schemaClassMirror.withAnnotation(SchemaAnnotationMirror(concepts = schemaConceptClassMirrors))
            }

            schemaFunctionMirrors.forEach { schemaMethod ->
                schemaClassMirror = schemaClassMirror.withMethod(schemaMethod)
            }

            return schemaClassMirror
        }
    }

    @SchemaDslMarker
    class ConceptDsl(conceptClassName: String) {
        var conceptMirror: FakeClassMirror = FakeClassMirror
            .interfaceMirror(conceptClassName)
            .withPackage(DEFAULT_PACKAGE_NAME)
        private val conceptFacetClassMirrors: MutableList<FakeClassMirror> = mutableListOf()
        private val conceptFunctionMirrors: MutableList<FakeFunctionMirror> = mutableListOf()

        fun setConceptIsClass() {
            conceptMirror = conceptMirror.setIsClass()
        }

        fun withAnnotationOnConcept(annotation: AnnotationMirror) {
            conceptMirror = conceptMirror.withAnnotation(annotation)
        }

        fun withConceptClassName(conceptClassName: String) {
            conceptMirror = conceptMirror.withClassName(conceptClassName)
        }

        fun withSuperClassMirror(superClassMirror: FakeClassMirror) {
            conceptMirror = conceptMirror.withSuperClass(superClassMirror)
        }

        fun facet(addFacetToConcept: Boolean = true, facetConfiguration: FacetDsl.() -> Unit): FakeClassMirror {
            val facetDsl = FacetDsl("Facet${conceptFacetClassMirrors.size}")
            facetConfiguration.invoke(facetDsl)
            val facetClassMirror = facetDsl.buildFacetMirror()
            if(addFacetToConcept) {
                conceptFacetClassMirrors.add(facetClassMirror)
            }
            return facetClassMirror
        }

        fun method(methodConfiguration: MethodDsl.() -> Unit): FakeFunctionMirror {
            val methodDsl = MethodDsl("Method${conceptFunctionMirrors.size}")
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            conceptFunctionMirrors.add(methodMirror)
            return methodMirror
        }


        fun buildConceptMirror(addConceptAnnotationWithAllFacets: Boolean = true): FakeClassMirror {
            if(addConceptAnnotationWithAllFacets) {
                conceptMirror = conceptMirror.withAnnotation(ConceptAnnotationMirror(facets = conceptFacetClassMirrors))
            }
            conceptFunctionMirrors.forEach { schemaMethod ->
                conceptMirror = conceptMirror.withMethod(schemaMethod)
            }
            return conceptMirror
        }
    }


    @SchemaDslMarker
    class MethodDsl(methodName: String) {
        private var functionMirror: FakeFunctionMirror = FakeFunctionMirror.methodMirror(methodName)

        fun withMethodName(methodName: String) {
            functionMirror = functionMirror.withMethodName(methodName)
        }

        fun withReturnType(returnType: FakeClassMirror, nullable: Boolean = false, vararg parameterAnnotations: AnnotationMirror) {
            functionMirror = functionMirror.withReturnType(
                returnClass = returnType,
                nullable = nullable,
                returnTypeAnnotations = parameterAnnotations
            )
        }

        fun withParameter(parameterName: String, parameterClassMirror: FakeClassMirror, nullable: Boolean = false, vararg parameterAnnotation: AnnotationMirror) {
            functionMirror = functionMirror.withParameter(
                parameterName = parameterName,
                parameterClass = parameterClassMirror,
                nullable = nullable,
                parameterAnnotation = parameterAnnotation
            )
        }

        fun withAnnotationOnMethod(annotation: AnnotationMirror) {
            functionMirror = functionMirror.withAnnotation(annotation)
        }


        fun buildMethodMirror(): FakeFunctionMirror {
            return functionMirror
        }
    }

    @SchemaDslMarker
    class FacetDsl(facetClassName: String) {
        var facetMirror: FakeClassMirror = FakeClassMirror
            .interfaceMirror(facetClassName)
            .withPackage(DEFAULT_PACKAGE_NAME)

        fun setFacetIsNotInterface() {
            facetMirror = facetMirror.setIsClass()
        }

        fun withAnnotationOnFacet(annotation: AnnotationMirror) {
            facetMirror = facetMirror.withAnnotation(annotation)
        }

        fun withFacetClassName(conceptClassName: String) {
            facetMirror = facetMirror.withClassName(conceptClassName)
        }

        fun withSuperClassMirrorForFacet(superClassMirror: FakeClassMirror) {
            facetMirror = facetMirror.withSuperClass(superClassMirror)
        }

        fun buildFacetMirror(): FakeClassMirror {
            return facetMirror
        }

    }

    fun schema(addSchemaAnnotationWithAllConcepts: Boolean = true, configuration: SchemaDsl.() -> Unit): FakeClassMirror {
        val schemaDsl = SchemaDsl()
        configuration.invoke(schemaDsl)
        return schemaDsl.buildSchemaMirror(addSchemaAnnotationWithAllConcepts)
    }

    fun concept(addConceptAnnotationWithAllFacets: Boolean = true, configuration: ConceptDsl.() -> Unit): FakeClassMirror {
        val conceptBuilder = ConceptDsl("Concept")
        configuration.invoke(conceptBuilder)
        return conceptBuilder.buildConceptMirror(addConceptAnnotationWithAllFacets = addConceptAnnotationWithAllFacets)
    }

}