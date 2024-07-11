package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.schemacreator.CommonMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.ClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptIdentifierValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryFacetValueAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.ReferenceFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ConceptQueryValidatorTest {

    @Test
    fun `test concept without accessor method should return without exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                // concept without facets and accessors
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with a unannotated method should throw an exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonMirrors.listOfAnyClassMirror())
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }


    @Test
    fun `test concept with a valid annotated method should return without exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                val facetInterfaceMirror = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonMirrors.listOfAnyClassMirror())
                    withAnnotationOnMethod(QueryFacetValueAnnotationMirror(facetInterfaceMirror))
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with a valid annotated concept id accessor method should return without exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getConceptId")
                    withReturnType(CommonMirrors.conceptIdentifierClassMirror())
                    withAnnotationOnMethod(QueryConceptIdentifierValueAnnotationMirror())
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with a unsupported facet class should throw an exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                val declaredFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                val undeclaredFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonMirrors.listOfAnyClassMirror())
                    withAnnotationOnMethod(QueryFacetValueAnnotationMirror(undeclaredFacet))
                }

                withAnnotationOnConcept(ConceptAnnotationMirror(listOf(declaredFacet)))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test concept with valid return types should return without exception`() {
        val commonInterface = ClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = SchemaMirrorDsl.schema {
            val anotherConcept = concept {
                withSuperClassMirror(commonInterface)
            }
            concept {
                val oneFacet = facet {
                    withAnnotationOnFacet(ReferenceFacetAnnotationMirror(listOf(commonInterface)))
                }

                val returnTypes = mapOf(
                    "Any" to CommonMirrors.anyClassMirror(),
                    "OtherConcept" to anotherConcept,
                    "CommonConceptInterface" to commonInterface,
                )
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAs$text")
                        withReturnType(returnType, nullable = false)
                        withAnnotationOnMethod(QueryFacetValueAnnotationMirror(oneFacet))
                    }
                }
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAsNullable$text")
                        withReturnType(returnType, nullable = true)
                        withAnnotationOnMethod(QueryFacetValueAnnotationMirror(oneFacet))
                    }
                }
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAsListOf$text")
                        withReturnType(CommonMirrors.listOfMirror(returnType))
                        withAnnotationOnMethod(QueryFacetValueAnnotationMirror(oneFacet))
                    }
                }
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAsSetOf$text")
                        withReturnType(CommonMirrors.setOfMirror(returnType))
                        withAnnotationOnMethod(QueryFacetValueAnnotationMirror(oneFacet))
                    }
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with method having parameters should throw an exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                val oneFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("myMethodWithParameter")
                    withReturnType(CommonMirrors.listOfAnyClassMirror())
                    withParameter("myParam", CommonMirrors.intClassMirror(), nullable = false)
                    withAnnotationOnMethod(QueryFacetValueAnnotationMirror(oneFacet))
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    @Disabled("Currently, this can't be validated properly for generic return types like list of strings etc.")
    fun `test concept with wrong return type in query method should throw an exception`() {
        val schemaMirror = SchemaMirrorDsl.schema {
            concept {
                val stringFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("myIntReturningMethod")
                    withReturnType(CommonMirrors.intClassMirror(), nullable = false)
                    withAnnotationOnMethod(QueryFacetValueAnnotationMirror(stringFacet))
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

}