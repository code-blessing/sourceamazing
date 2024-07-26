package org.codeblessing.sourceamazing.schema.schemacreator

import org.codeblessing.sourceamazing.schema.schemacreator.CommonMirrors.DEFAULT_PACKAGE_NAME
import org.codeblessing.sourceamazing.schema.typemirror.AnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FunctionMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.TypeMirror

object SchemaMirrorDsl {
    @DslMarker
    annotation class SchemaDslMarker

    @SchemaDslMarker
    class SchemaDsl {
        private var schemaClassMirror: ClassMirror = ClassMirror
            .interfaceMirror("Schema")
            .setIsInterface()
            .withPackage(DEFAULT_PACKAGE_NAME)


        private val schemaConceptClassMirrors: MutableList<ClassMirror> = mutableListOf()

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


        fun withSuperClassMirror(superClassMirror: ClassMirror) {
            schemaClassMirror = schemaClassMirror.withSuperClass(superClassMirror)
        }


        fun concept(addConceptAnnotationWithAllFacets: Boolean = true, configuration: ConceptDsl.() -> Unit): ClassMirror {
            val conceptDsl = ConceptDsl("Concept${schemaConceptClassMirrors.size}")
            configuration.invoke(conceptDsl)
            val conceptClassMirror = conceptDsl.buildConceptMirror(addConceptAnnotationWithAllFacets)
            schemaConceptClassMirrors.add(conceptClassMirror)
            return conceptClassMirror
        }

        private val schemaFunctionMirrors: MutableList<FunctionMirror> = mutableListOf()

        fun method(methodConfiguration: MethodDsl.() -> Unit): FunctionMirror {
            val methodDsl = MethodDsl("Method${schemaFunctionMirrors.size}")
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            schemaFunctionMirrors.add(methodMirror)
            return methodMirror
        }

        fun buildSchemaMirror(addSchemaAnnotationWithAllConcepts: Boolean): ClassMirror {
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
        var conceptMirror: ClassMirror = ClassMirror
            .interfaceMirror(conceptClassName)
            .withPackage(DEFAULT_PACKAGE_NAME)
        private val conceptFacetClassMirrors: MutableList<ClassMirror> = mutableListOf()
        private val conceptFunctionMirrors: MutableList<FunctionMirror> = mutableListOf()

        fun setConceptIsClass() {
            conceptMirror = conceptMirror.setIsClass()
        }

        fun withAnnotationOnConcept(annotation: AnnotationMirror) {
            conceptMirror = conceptMirror.withAnnotation(annotation)
        }

        fun withConceptClassName(conceptClassName: String) {
            conceptMirror = conceptMirror.withClassName(conceptClassName)
        }

        fun withSuperClassMirror(superClassMirror: ClassMirror) {
            conceptMirror = conceptMirror.withSuperClass(superClassMirror)
        }

        fun facet(addFacetToConcept: Boolean = true, facetConfiguration: FacetDsl.() -> Unit): ClassMirror {
            val facetDsl = FacetDsl("Facet${conceptFacetClassMirrors.size}")
            facetConfiguration.invoke(facetDsl)
            val facetClassMirror = facetDsl.buildFacetMirror()
            if(addFacetToConcept) {
                conceptFacetClassMirrors.add(facetClassMirror)
            }
            return facetClassMirror
        }

        fun method(methodConfiguration: MethodDsl.() -> Unit): FunctionMirror {
            val methodDsl = MethodDsl("Method${conceptFunctionMirrors.size}")
            methodConfiguration.invoke(methodDsl)
            val methodMirror = methodDsl.buildMethodMirror()
            conceptFunctionMirrors.add(methodMirror)
            return methodMirror
        }


        fun buildConceptMirror(addConceptAnnotationWithAllFacets: Boolean = true): ClassMirror {
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

        fun withAnnotationOnMethod(annotation: AnnotationMirror) {
            functionMirror = functionMirror.withAnnotation(annotation)
        }


        fun buildMethodMirror(): FunctionMirror {
            return functionMirror
        }
    }

    @SchemaDslMarker
    class FacetDsl(facetClassName: String) {
        var facetMirror: ClassMirror = ClassMirror
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

        fun withSuperClassMirrorForFacet(superClassMirror: ClassMirror) {
            facetMirror = facetMirror.withSuperClass(superClassMirror)
        }

        fun buildFacetMirror(): ClassMirror {
            return facetMirror
        }

    }

    fun schema(addSchemaAnnotationWithAllConcepts: Boolean = true, configuration: SchemaDsl.() -> Unit): ClassMirror {
        val schemaDsl = SchemaDsl()
        configuration.invoke(schemaDsl)
        return schemaDsl.buildSchemaMirror(addSchemaAnnotationWithAllConcepts)
    }

    fun concept(addConceptAnnotationWithAllFacets: Boolean = true, configuration: ConceptDsl.() -> Unit): ClassMirror {
        val conceptBuilder = ConceptDsl("Concept")
        configuration.invoke(conceptBuilder)
        return conceptBuilder.buildConceptMirror(addConceptAnnotationWithAllFacets = addConceptAnnotationWithAllFacets)
    }

}