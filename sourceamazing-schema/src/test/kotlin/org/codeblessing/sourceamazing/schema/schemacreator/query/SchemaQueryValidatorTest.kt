package org.codeblessing.sourceamazing.schema.schemacreator.query

import org.codeblessing.sourceamazing.schema.api.annotations.QueryConcepts
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.schemacreator.exceptions.MalformedSchemaException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SchemaQueryValidatorTest {

    @Test
    fun `test schema without accessor method should return without exception`() {
        val commonInterface = FakeKClass.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema {
            concept {
                withSuperClassMirror(commonInterface)
            }
            concept {
                withSuperClassMirror(commonInterface)
            }
        }

        SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
    }

    @Test
    fun `test schema with a unannotated method should throw an exception`() {
        val commonInterface = FakeKClass.interfaceMirror("CommonInterface").setIsInterface()
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
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test schema with a unsupported concept class should throw an exception`() {
        val commonInterface = FakeKClass.interfaceMirror("CommonInterface").setIsInterface()
        val schemaMirror = FakeSchemaMirrorDsl.schema(addSchemaAnnotationWithAllConcepts = false) {
            val declaredConceptClassMirror = concept {
                withSuperClassMirror(commonInterface)
            }
            val undeclaredConceptClassMirror = concept {
                withSuperClassMirror(commonInterface)
            }

            withAnnotationOnSchema(Schema(arrayOf(declaredConceptClassMirror)))

            method {
                withMethodName("getMyConcepts")
                withReturnType(CommonFakeMirrors.listOfAnyClassMirror())
                withAnnotationOnMethod(QueryConcepts(arrayOf(declaredConceptClassMirror, undeclaredConceptClassMirror)))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test schema with a empty concept class list should throw an exception`() {
        val commonInterface = FakeKClass.interfaceMirror("CommonInterface").setIsInterface()
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
                withAnnotationOnMethod(QueryConcepts(emptyArray()))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

    @Test
    fun `test schema with valid return types should return without exception`() {
        val commonInterface = FakeKClass.interfaceMirror("CommonInterface").setIsInterface()
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
                    withAnnotationOnMethod(QueryConcepts(arrayOf(oneConcept)))
                }
            }
            for ((text, returnType) in returnTypes) {
                method {
                    withMethodName("getConceptAsNullable$text")
                    withReturnType(returnType, nullable = true)
                    withAnnotationOnMethod(QueryConcepts(arrayOf(oneConcept)))
                }
            }
            for ((text, returnType) in returnTypes) {
                method {
                    withMethodName("getConceptAsListOf$text")
                    withReturnType(CommonFakeMirrors.listOfMirror(returnType))
                    withAnnotationOnMethod(QueryConcepts(arrayOf(oneConcept)))
                }
            }
            for ((text, returnType) in returnTypes) {
                method {
                    withMethodName("getConceptAsSetOf$text")
                    withReturnType(CommonFakeMirrors.setOfMirror(returnType))
                    withAnnotationOnMethod(QueryConcepts(arrayOf(oneConcept)))
                }
            }
        }

        SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
    }

    @Test
    fun `test schema with method having parameters should throw an exception`() {
        val commonInterface = FakeKClass.interfaceMirror("CommonInterface").setIsInterface()
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
                withAnnotationOnMethod(QueryConcepts(arrayOf(oneConcept, anotherConcept)))
            }
        }

        assertThrows(MalformedSchemaException::class.java) {
            SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
        }
    }

}