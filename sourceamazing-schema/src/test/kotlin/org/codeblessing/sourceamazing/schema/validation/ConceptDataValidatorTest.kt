package org.codeblessing.sourceamazing.schema.validation

import org.codeblessing.sourceamazing.schema.ConceptName
import org.codeblessing.sourceamazing.schema.FacetName
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataImpl
import org.codeblessing.sourceamazing.schema.datacollection.validation.ConceptDataValidator
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.DuplicateConceptIdentifierException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.MissingReferencedConceptFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.UnknownConceptException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.UnknownFacetNameException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongCardinalityForFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongReferencedConceptFacetValueException
import org.codeblessing.sourceamazing.schema.datacollection.validation.exceptions.WrongTypeForFacetValueException
import org.codeblessing.sourceamazing.schema.fakereflection.FakeKClass
import org.codeblessing.sourceamazing.schema.schemacreator.CommonFakeMirrors
import org.codeblessing.sourceamazing.schema.schemacreator.FakeSchemaMirrorDsl
import org.codeblessing.sourceamazing.schema.schemacreator.SchemaCreator
import org.codeblessing.sourceamazing.schema.toConceptName
import org.codeblessing.sourceamazing.schema.toFacetName
import org.codeblessing.sourceamazing.schema.typemirror.EnumFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.FakeClassMirror
import org.codeblessing.sourceamazing.schema.typemirror.ReferenceFacetAnnotationMirror
import org.codeblessing.sourceamazing.schema.typemirror.StringFacetAnnotationMirror
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConceptDataValidatorTest {

    private enum class MyEnumeration { X,Y }
    private enum class OtherEnumeration { Y,Z }

    @Nested
    @DisplayName("basic concept validation")
    inner class BasicConceptValidationTests {
        @Test
        fun `validate an empty list does return without exception`() {
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                concept {
                    // empty concept
                }
            }
            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            ConceptDataValidator.validateEntries(schemaAccess, emptyList())
        }

        @Test
        fun `validate a unknown concept throws an exception`() {
            val undeclaredConcept = FakeSchemaMirrorDsl.concept {
                facet {
                    withAnnotationOnFacet(StringFacet(minimumOccurrences = 0, maximumOccurrences = 1))
                }
            }

            val schemaMirror = FakeSchemaMirrorDsl.schema {
                concept {
                    // empty concept
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(undeclaredConcept) // here we define undeclaredConcept which is not known in this schema
            Assertions.assertThrows(UnknownConceptException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }

        @Test
        fun `validate a duplicate concept throws an exception`() {
            lateinit var conceptClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    // empty concept
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptDataOriginal = createEmptyConceptData(conceptClassMirror)
            val conceptDataDuplicate = createEmptyConceptData(conceptClassMirror)
            Assertions.assertThrows(DuplicateConceptIdentifierException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataOriginal, conceptDataDuplicate))
            }
        }
    }

    @Nested
    @DisplayName("basic facet validation")
    inner class BasicFacetValidationTests {
        @Test
        fun `validate unknown facet throws an exception`() {
            lateinit var conceptClassMirror: FakeKClass
            lateinit var knownTextFacetClassMirror: FakeKClass
            lateinit var unknownTextFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    knownTextFacetClassMirror = facet {
                        withAnnotationOnFacet(StringFacet())
                    }

                    unknownTextFacetClassMirror = facet(addFacetToConcept = false) {
                        withAnnotationOnFacet(StringFacet())
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(knownTextFacetClassMirror.toFacetName(), "my text")
            conceptData.addFacetValue(unknownTextFacetClassMirror.toFacetName(), "my text") // here we add values for an unknown facet
            Assertions.assertThrows(UnknownFacetNameException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }

        @Test
        fun `validate a valid entry does return without exception`() {
            lateinit var conceptClassMirror: FakeKClass
            lateinit var knownTextFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    knownTextFacetClassMirror = facet {
                        withAnnotationOnFacet(StringFacet())
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(knownTextFacetClassMirror.toFacetName(), "my text")
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }

        @Test
        fun `validate a entry with wrong type does throw an exception`() {
            lateinit var conceptClassMirror: FakeKClass
            lateinit var textFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    textFacetClassMirror = facet {
                        withAnnotationOnFacet(StringFacet())
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(textFacetClassMirror.toFacetName(), 42) // here we add a number instead of text
            Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }
    }

    @Nested
    @DisplayName("basic facet validation")
    inner class FacetCardinalityValidationTests {

        @Test
        fun `validate missing mandatory text facet throws an exception`() {
            lateinit var conceptClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    facet {
                        withAnnotationOnFacet(StringFacet(minimumOccurrences = 1, maximumOccurrences = 1))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            // here we do not add the mandatory text facet
            Assertions.assertThrows(WrongCardinalityForFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }

        @Test
        fun `validate too much values on facet throws an exception`() {
            lateinit var conceptClassMirror: FakeKClass
            lateinit var textFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    textFacetClassMirror = facet {
                        withAnnotationOnFacet(StringFacet(minimumOccurrences = 1, maximumOccurrences = 1))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(textFacetClassMirror.toFacetName(), "my text")
            conceptData.addFacetValue(textFacetClassMirror.toFacetName(), "my text number two")
            Assertions.assertThrows(WrongCardinalityForFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }
    }

    @Nested
    @DisplayName("enum facet validation")
    inner class EnumFacetValidationTests {

        @Test
        fun `validate that a correct enum as String does return without exception`() {
            val enumClassMirror = CommonFakeMirrors.namedEnumClassMirror(className = "MyEnum", "X", "Y")
            lateinit var conceptClassMirror: FakeKClass
            lateinit var enumFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    enumFacetClassMirror = facet {
                        withAnnotationOnFacet(EnumFacet(minimumOccurrences = 1, maximumOccurrences = 5, enumerationClass = enumClassMirror))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), "X")
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), "Y")
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), "X")
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }

        @Test
        fun `validate that a correct enum does return without exception`() {
            val enumClassMirror = CommonFakeMirrors.namedEnumClassMirror(className = "MyEnumeration", "X", "Y")
            lateinit var conceptClassMirror: FakeKClass
            lateinit var enumFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    enumFacetClassMirror = facet {
                        withAnnotationOnFacet(EnumFacet(minimumOccurrences = 1, maximumOccurrences = 5, enumerationClass = enumClassMirror))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), MyEnumeration.X)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), MyEnumeration.Y)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), MyEnumeration.X)
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }

        @Test
        fun `validate that another enum type with valid enum values does return without exception`() {
            val enumClassMirror = CommonFakeMirrors.namedEnumClassMirror(className = "MyEnumeration", "X", "Y")
            lateinit var conceptClassMirror: FakeKClass
            lateinit var enumFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    enumFacetClassMirror = facet {
                        withAnnotationOnFacet(EnumFacet(minimumOccurrences = 1, maximumOccurrences = 5, enumerationClass = enumClassMirror))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), MyEnumeration.X)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), OtherEnumeration.Y)
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
        }

        @Test
        fun `validate that a wrong enum type as string throws an exception`() {
            val enumClassMirror = CommonFakeMirrors.namedEnumClassMirror(className = "MyEnum", "X", "Y")
            lateinit var conceptClassMirror: FakeKClass
            lateinit var enumFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    enumFacetClassMirror = facet {
                        withAnnotationOnFacet(EnumFacet(minimumOccurrences = 1, maximumOccurrences = 5, enumerationClass = enumClassMirror))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), "X")
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), "y")  // lowercase is wrong

            Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }

        @Test
        fun `validate that a wrong enum type throws an exception`() {
            val enumClassMirror = CommonFakeMirrors.namedEnumClassMirror(className = "MyEnum", "X", "Y")
            lateinit var conceptClassMirror: FakeKClass
            lateinit var enumFacetClassMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                conceptClassMirror = concept {
                    enumFacetClassMirror = facet {
                        withAnnotationOnFacet(EnumFacet(minimumOccurrences = 1, maximumOccurrences = 5, enumerationClass = enumClassMirror))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassMirror)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), MyEnumeration.X)
            conceptData.addFacetValue(enumFacetClassMirror.toFacetName(), OtherEnumeration.Z)

            Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }
    }

    @Nested
    @DisplayName("reference facet validation")
    inner class ReferenceFacetValidationTests {

        @Test
        fun `validate that a wrong type for reference throws an exception`() {
            lateinit var conceptClassWithReferenceFacetMirror: FakeKClass
            lateinit var referenceFacetMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                val otherConceptMirror = concept {
                    // other concept
                }
                conceptClassWithReferenceFacetMirror = concept {
                    referenceFacetMirror = facet {
                        withAnnotationOnFacet(ReferenceFacet(referencedConcepts = arrayOf(otherConceptMirror)))
                    }
                }
            }

            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptData = createEmptyConceptData(conceptClassWithReferenceFacetMirror)

            conceptData.addFacetValue(referenceFacetMirror.toFacetName(), "Bar")
            Assertions.assertThrows(WrongTypeForFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptData))
            }
        }

        @Test
        fun `validate that a reference pointing to a missing concept throws an exception`() {
            lateinit var conceptClassWithReferenceFacetMirror: FakeKClass
            lateinit var referenceFacetMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                val otherConceptMirror = concept {
                    // other concept
                }
                conceptClassWithReferenceFacetMirror = concept {
                    referenceFacetMirror = facet {
                        withAnnotationOnFacet(ReferenceFacet(referencedConcepts = arrayOf(otherConceptMirror)))
                    }
                }
            }

            val conceptIdentifier = ConceptIdentifier.of("Bar")
            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)
            val conceptDataReferencing = createEmptyConceptData(conceptClassWithReferenceFacetMirror)
            conceptDataReferencing.addFacetValue(referenceFacetMirror.toFacetName(), conceptIdentifier)

            Assertions.assertThrows(MissingReferencedConceptFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataReferencing))
            }
        }

        @Test
        fun `validate that a reference pointing to an available concept does return without exception`() {
            lateinit var conceptClassWithReferenceFacetMirror: FakeKClass
            lateinit var otherConceptMirror: FakeKClass
            lateinit var referenceFacetMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                otherConceptMirror = concept {
                    // other concept
                }
                conceptClassWithReferenceFacetMirror = concept {
                    referenceFacetMirror = facet {
                        withAnnotationOnFacet(ReferenceFacet(referencedConcepts = arrayOf(otherConceptMirror)))
                    }
                }
            }

            val conceptIdentifier = ConceptIdentifier.of("Bar")
            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)

            val conceptDataReferencing = createEmptyConceptData(conceptClassWithReferenceFacetMirror)
            conceptDataReferencing.addFacetValue(referenceFacetMirror.toFacetName(), conceptIdentifier)
            val conceptDataReferenced = createEmptyConceptData(otherConceptMirror, conceptIdentifier)
            ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataReferencing, conceptDataReferenced))
        }

        @Test
        fun `validate that a reference pointing to an available concept with wrong type throws an exception`() {
            lateinit var conceptClassWithReferenceFacetMirror: FakeKClass
            lateinit var otherConceptMirror: FakeKClass
            lateinit var otherThanOtherConceptMirror: FakeKClass
            lateinit var referenceFacetMirror: FakeKClass
            val schemaMirror = FakeSchemaMirrorDsl.schema {
                otherConceptMirror = concept {
                    // other concept
                }
                otherThanOtherConceptMirror = concept {
                    // other than other concept
                }
                conceptClassWithReferenceFacetMirror = concept {
                    referenceFacetMirror = facet {
                        withAnnotationOnFacet(ReferenceFacet(referencedConcepts = arrayOf(otherConceptMirror)))
                    }
                }
            }

            val conceptIdentifier = ConceptIdentifier.of("Bar")
            val schemaAccess = SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaMirror)

            val conceptDataReferencing = createEmptyConceptData(conceptClassWithReferenceFacetMirror)
            conceptDataReferencing.addFacetValue(referenceFacetMirror.toFacetName(), conceptIdentifier)
            val conceptDataReferenced = createEmptyConceptData(otherThanOtherConceptMirror, conceptIdentifier) // wrong concept type
            Assertions.assertThrows(WrongReferencedConceptFacetValueException::class.java) {
                ConceptDataValidator.validateEntries(schemaAccess, listOf(conceptDataReferencing, conceptDataReferenced))
            }
        }
    }



    private fun createEmptyConceptData(conceptClass: FakeKClass, conceptIdentifier: ConceptIdentifier = ConceptIdentifier.of("Foo")): ConceptDataImpl {
        return ConceptDataImpl(
            1,
            conceptClass.toConceptName(),
            conceptIdentifier
        )

    }
}