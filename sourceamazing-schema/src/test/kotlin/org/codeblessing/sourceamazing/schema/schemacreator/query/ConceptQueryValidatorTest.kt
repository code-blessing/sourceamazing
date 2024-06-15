package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.ConceptAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
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
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                // concept without facets and accessors
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with a unannotated method should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }


    @Test
    fun `test concept with a valid annotated method should return without exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                val facetInterfaceMirror = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                    withAnnotationOnMethod(QueryFacetValueAnnotationMirror(facetInterfaceMirror))
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with a valid annotated concept id accessor method should return without exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getConceptId")
                    withReturnType(CommonFakeMirrors.conceptIdentifierClassMirror())
                    withAnnotationOnMethod(QueryConceptIdentifierValueAnnotationMirror())
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with a unsupported facet class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                val declaredFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                val undeclaredFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
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
        val commonInterface = FakeClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            val otherConcept = concept {
                withSuperClassMirror(commonInterface)
            }
            concept {
                withSuperClassMirror(commonInterface)
                val oneFacet = facet {
                    withAnnotationOnFacet(ReferenceFacetAnnotationMirror(listOf(otherConcept)))
                }

                val returnTypes = mapOf(
                    "Any" to CommonFakeMirrors.anyClassMirror(),
                    "OtherConcept" to otherConcept,
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
                        withReturnType(CommonFakeMirrors.listOfMirror(returnType))
                        withAnnotationOnMethod(QueryFacetValueAnnotationMirror(oneFacet))
                    }
                }
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAsSetOf$text")
                        withReturnType(CommonFakeMirrors.setOfMirror(returnType))
                        withAnnotationOnMethod(QueryFacetValueAnnotationMirror(oneFacet))
                    }
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test concept with method having parameters should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                val oneFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("myMethodWithParameter")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                    withParameter("myParam", CommonFakeMirrors.intClassMirror(), nullable = false)
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
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                val stringFacet = facet {
                    withAnnotationOnFacet(StringFacetAnnotationMirror())
                }

                method {
                    withMethodName("myIntReturningMethod")
                    withReturnType(CommonFakeMirrors.intClassMirror(), nullable = false)
                    withAnnotationOnMethod(QueryFacetValueAnnotationMirror(stringFacet))
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

}