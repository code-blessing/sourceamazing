package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.QueryConceptsAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.SchemaAnnotationMirror
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SchemaQueryValidatorTest {

    @Test
    fun `test schema without accessor method should return without exception`() {
        val commonInterface = FakeClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                withSuperClassMirror(commonInterface)
            }
            concept {
                withSuperClassMirror(commonInterface)
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test schema with a unannotated method should throw an exception`() {
        val commonInterface = FakeClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                withSuperClassMirror(commonInterface)
            }
            concept {
                withSuperClassMirror(commonInterface)
            }

            method {
                withMethodName("getMyConcepts")
                withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                // no annotation on method
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema with a unsupported concept class should throw an exception`() {
        val commonInterface = FakeClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema(addSchemaAnnotationWithAllConcepts = false) {
            val declaredConceptClassMirror = concept {
                withSuperClassMirror(commonInterface)
            }
            val undeclaredConceptClassMirror = concept {
                withSuperClassMirror(commonInterface)
            }

            withAnnotationOnSchema(SchemaAnnotationMirror(listOf(declaredConceptClassMirror)))

            method {
                withMethodName("getMyConcepts")
                withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                withAnnotationOnMethod(QueryConceptsAnnotationMirror(listOf(declaredConceptClassMirror, undeclaredConceptClassMirror)))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema with a empty concept class list should throw an exception`() {
        val commonInterface = FakeClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                withSuperClassMirror(commonInterface)
            }
            concept {
                withSuperClassMirror(commonInterface)
            }

            method {
                withMethodName("getMyConcepts")
                withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                withAnnotationOnMethod(QueryConceptsAnnotationMirror(emptyList()))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

    @Test
    fun `test schema with valid return types should return without exception`() {
        val commonInterface = FakeClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            val oneConcept = concept {
                withSuperClassMirror(commonInterface)
            }
            val returnTypes = mapOf(
                "Any" to CommonFakeMirrors.anyClassMirror(),
                "OneConcept" to oneConcept,
                "CommonConceptInterface" to commonInterface,
            )
            for ((text, returnType) in returnTypes) {
                method {
                    withMethodName("getConceptAs$text")
                    withReturnType(returnType, nullable = false)
                    withAnnotationOnMethod(QueryConceptsAnnotationMirror(listOf(oneConcept)))
                }
            }
            for ((text, returnType) in returnTypes) {
                method {
                    withMethodName("getConceptAsNullable$text")
                    withReturnType(returnType, nullable = true)
                    withAnnotationOnMethod(QueryConceptsAnnotationMirror(listOf(oneConcept)))
                }
            }
            for ((text, returnType) in returnTypes) {
                method {
                    withMethodName("getConceptAsListOf$text")
                    withReturnType(CommonFakeMirrors.listOfMirror(returnType))
                    withAnnotationOnMethod(QueryConceptsAnnotationMirror(listOf(oneConcept)))
                }
            }
            for ((text, returnType) in returnTypes) {
                method {
                    withMethodName("getConceptAsSetOf$text")
                    withReturnType(CommonFakeMirrors.setOfMirror(returnType))
                    withAnnotationOnMethod(QueryConceptsAnnotationMirror(listOf(oneConcept)))
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
    }

    @Test
    fun `test schema with method having parameters should throw an exception`() {
        val commonInterface = FakeClassMirror.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            val oneConcept = concept {
                withSuperClassMirror(commonInterface)
            }
            val anotherConcept = concept {
                withSuperClassMirror(commonInterface)
            }

            method {
                withMethodName("getMyConceptsAsListOfAny")
                withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                withParameter("myParam", CommonFakeMirrors.intClassMirror(), nullable = false)
                withAnnotationOnMethod(QueryConceptsAnnotationMirror(listOf(oneConcept, anotherConcept)))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaClassMirror(schemaMirror)
        }
    }

}