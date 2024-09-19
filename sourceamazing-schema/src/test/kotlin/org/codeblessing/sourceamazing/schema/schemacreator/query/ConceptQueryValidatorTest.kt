package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.QueryConceptIdentifierValue
import org.codeblessing.sourceamazing.schema.api.annotations.QueryFacetValue
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
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

        SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
    }

    @Test
    fun `test concept with a unannotated method should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacet())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }


    @Test
    fun `test concept with a valid annotated method should return without exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                val facetInterfaceMirror = facet {
                    withAnnotationOnFacet(StringFacet())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                    withAnnotationOnMethod(QueryFacetValue(facetInterfaceMirror))
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
    }

    @Test
    fun `test concept with a valid annotated concept id accessor method should return without exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                facet {
                    withAnnotationOnFacet(StringFacet())
                }

                method {
                    withMethodName("getConceptId")
                    withReturnType(CommonFakeMirrors.conceptIdentifierClassMirror())
                    withAnnotationOnMethod(QueryConceptIdentifierValue())
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
    }

    @Test
    fun `test concept with a unsupported facet class should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept(addConceptAnnotationWithAllFacets = false) {
                val declaredFacet = facet {
                    withAnnotationOnFacet(StringFacet())
                }

                val undeclaredFacet = facet {
                    withAnnotationOnFacet(StringFacet())
                }

                method {
                    withMethodName("getFacetValue")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                    withAnnotationOnMethod(QueryFacetValue(undeclaredFacet))
                }

                withAnnotationOnConcept(Concept(arrayOf(declaredFacet)))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test concept with valid return types should return without exception`() {
        val commonInterface = FakeKClass.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            val otherConcept = concept {
                withSuperClassMirror(commonInterface)
            }
            concept {
                withSuperClassMirror(commonInterface)
                val oneFacet = facet {
                    withAnnotationOnFacet(ReferenceFacet(arrayOf(otherConcept)))
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
                        withAnnotationOnMethod(QueryFacetValue(oneFacet))
                    }
                }
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAsNullable$text")
                        withReturnType(returnType, nullable = true)
                        withAnnotationOnMethod(QueryFacetValue(oneFacet))
                    }
                }
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAsListOf$text")
                        withReturnType(CommonFakeMirrors.listOfMirror(returnType))
                        withAnnotationOnMethod(QueryFacetValue(oneFacet))
                    }
                }
                for ((text, returnType) in returnTypes) {
                    method {
                        withMethodName("getConceptAsSetOf$text")
                        withReturnType(CommonFakeMirrors.setOfMirror(returnType))
                        withAnnotationOnMethod(QueryFacetValue(oneFacet))
                    }
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
    }

    @Test
    fun `test concept with method having parameters should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                val oneFacet = facet {
                    withAnnotationOnFacet(StringFacet())
                }

                method {
                    withMethodName("myMethodWithParameter")
                    withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                    withParameter("myParam", CommonFakeMirrors.intClassMirror(), nullable = false)
                    withAnnotationOnMethod(QueryFacetValue(oneFacet))
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    @Disabled("Currently, this can't be validated properly for generic return types like list of strings etc.")
    fun `test concept with wrong return type in query method should throw an exception`() {
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                val stringFacet = facet {
                    withAnnotationOnFacet(StringFacet())
                }

                method {
                    withMethodName("myIntReturningMethod")
                    withReturnType(CommonFakeMirrors.intClassMirror(), nullable = false)
                    withAnnotationOnMethod(QueryFacetValue(stringFacet))
                }
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

}