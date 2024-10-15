package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.IgnoreNullFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BuilderMethodApiParameterTypesTest {

    @Schema(concepts = [SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class])
    private interface SchemaWithConceptWithTextFacet {
        enum class MyEnum {
            @Suppress("UNUSED") A,
            @Suppress("UNUSED") B,

        }

        @Concept(facets = [
            ConceptWithTextFacet.TextFacet::class,
            ConceptWithTextFacet.BoolFacet::class,
            ConceptWithTextFacet.NumberFacet::class,
            ConceptWithTextFacet.EnumerationFacet::class,
            ConceptWithTextFacet.SelfRefFacet::class,
        ])
        interface ConceptWithTextFacet {
            @StringFacet
            interface TextFacet
            @BooleanFacet
            interface BoolFacet
            @IntFacet
            interface NumberFacet
            @EnumFacet(MyEnum::class)
            interface EnumerationFacet
            @ReferenceFacet([ConceptWithTextFacet::class])
            interface SelfRefFacet
        }
    }

    @Builder
    private interface BuilderMethodWithIllegalConceptIdClass {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptId: String,
        )
    }

    @Test
    fun `test concept id parameter without ConceptIdentifier class should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithIllegalConceptIdClass::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableConceptId {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @SetConceptIdentifierValue conceptId: ConceptIdentifier?,
        )
    }

    @Test
    fun `test concept id parameter as nullable type should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableConceptId::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableConceptIdAndIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        fun doSomething(
            @IgnoreNullFacetValue @SetConceptIdentifierValue conceptId: ConceptIdentifier?,
        )
    }

    @Test
    fun `test concept id parameter as nullable type with IgnoreNullFacetValue annotation should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableConceptIdAndIgnoreNullFacetValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithoutIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class) myText: String?,
        )
    }

    @Test
    fun `test string facet parameter as nullable type without IgnoreNullFacetValue should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableParameterWithoutIgnoreNullFacetValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableParameterWithIgnoreNullFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @IgnoreNullFacetValue
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class)
            myText: String?,
        )
    }

    @Test
    fun `test string facet parameter as nullable type with IgnoreNullFacetValue should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableParameterWithIgnoreNullFacetValueAnnotation::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCollectionTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class) myTexts: List<String>,
        )
    }

    @Test
    fun `test string facet parameter with collection type instead of string should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCollectionTypedStringParameter::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedStringParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.TextFacet::class) myText: Int,
        )
    }

    @Test
    fun `test string facet parameter with other type than string should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedStringParameter::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.BoolFacet::class) myBoolean: Int,
        )
    }

    @Test
    fun `test boolean facet parameter with other type than boolean should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedBooleanParameter::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithArrayOfBooleanParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.BoolFacet::class) myBooleans: Array<Boolean>,
        )
    }

    @Test
    fun `test boolean facet parameter with array of boolean instead of boolean should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithArrayOfBooleanParameter::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedIntParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.NumberFacet::class) myInt: String,
        )
    }

    @Test
    fun `test int facet parameter with other type than int should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedIntParameter::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.EnumerationFacet::class) myEnum: String,
        )
    }

    @Test
    fun `test enum facet parameter with other type than enum should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedEnumParameter::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.EnumerationFacet::class) myEnum: SchemaWithConceptWithTextFacet.MyEnum,
        )
    }

    @Test
    fun `test enum facet parameter should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithEnumParameter::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithSetOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.EnumerationFacet::class) myEnums: Set<SchemaWithConceptWithTextFacet.MyEnum>,
        )
    }

    @Test
    fun `test enum facet parameter with set of enum instead of single enum should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithSetOfEnumParameter::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithNullableSetOfEnumParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.EnumerationFacet::class) myEnums: Set<SchemaWithConceptWithTextFacet.MyEnum>?,
        )
    }

    @Test
    fun `test enum facet parameter with nullable set of enum instead of single enum should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithNullableSetOfEnumParameter::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithWrongTypedReferenceParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.SelfRefFacet::class) myRef: String,
        )
    }

    @Test
    fun `test reference facet parameter with other type than a ConceptIdentifier should throw an exception`() {
        assertThrows(BuilderMethodParameterSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithWrongTypedReferenceParameter::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithVarargArray {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.SelfRefFacet::class) vararg myRefs: ConceptIdentifier,
        )
    }

    @Test
    fun `test reference facet parameter with vararg array of ConceptIdentifier should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithVarargArray::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodWithCorrectReferenceTypeParameter {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithTextFacet::class)
        @SetRandomConceptIdentifierValue
        fun doSomething(
            @SetFacetValue(facetToModify = SchemaWithConceptWithTextFacet.ConceptWithTextFacet.SelfRefFacet::class) myRef: ConceptIdentifier,
        )
    }

    @Test
    fun `test reference facet parameter with correct ConceptIdentifier type should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodWithCorrectReferenceTypeParameter::class) { builder ->
                // do nothing
            }
        }
    }

}