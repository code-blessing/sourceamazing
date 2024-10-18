package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.Builder
import org.codeblessing.sourceamazing.builder.api.annotations.BuilderMethod
import org.codeblessing.sourceamazing.builder.api.annotations.ExpectedAliasFromSuperiorBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.InjectBuilder
import org.codeblessing.sourceamazing.builder.api.annotations.NewConcept
import org.codeblessing.sourceamazing.builder.api.annotations.SetAliasConceptIdentifierReferenceFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedBooleanFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedEnumFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedIntFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetFixedStringFacetValue
import org.codeblessing.sourceamazing.builder.api.annotations.SetRandomConceptIdentifierValue
import org.codeblessing.sourceamazing.builder.api.annotations.WithNewBuilder
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
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

class BuilderApiAliasTest {
    // Do not validate for duplicate assignment on the same facet and concept, as this can
    // be a useful case when adding values to a facet (instead of replacing values).

    @Schema(concepts = [SchemaWithConceptWithTextFacet.ConceptWithFacet::class])
    private interface SchemaWithConceptWithTextFacet {

        private enum class MyEnumeration {
            @Suppress("UNUSED") A,
            @Suppress("UNUSED") B,
            @Suppress("UNUSED") C,
        }

        @Concept(facets = [
            ConceptWithFacet.TextFacet::class,
            ConceptWithFacet.BoolFacet::class,
            ConceptWithFacet.NumberFacet::class,
            ConceptWithFacet.EnumerationFacet::class,
            ConceptWithFacet.RefFacet::class,
        ])
        interface ConceptWithFacet {
            @StringFacet
            interface TextFacet
            @BooleanFacet
            interface BoolFacet
            @IntFacet
            interface NumberFacet
            @EnumFacet(enumerationClass = MyEnumeration::class)
            interface EnumerationFacet
            @ReferenceFacet(referencedConcepts = [ConceptWithFacet::class])
            interface RefFacet
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForNewConcept {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for NewConcept annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForNewConcept::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(builderClass = BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewConcept::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewConcept.() -> Unit,
        )
        
        
        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewConcept {

            @Suppress("UNUSED")
            @BuilderMethod
            @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething()
        }
    }

    @Test
    fun `test duplicate alias from superior concept for NewConcept annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForRandomConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for SetRandomConceptIdentifierValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForRandomConceptIdentifier::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier2: ConceptIdentifier,
        )
    }

    @Test
    fun `test duplicate alias for ConceptIdentifierValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutConceptIdentifierForAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test missing concept identifier declaration for alias should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutConceptIdentifierForAlias::class) { builder ->
                    // do nothing
                }
            }
        }
    }


    @Builder
    private interface BuilderMethodWithDuplicateMixedConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier: ConceptIdentifier
        )
    }

    @Test
    fun `test duplicate alias with SetRandomConceptIdentifierValue and SetConceptIdentifierValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateMixedConceptIdentifier::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifier {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "unknown")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetRandomConceptIdentifierValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifier::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "unknown") id: ConceptIdentifier
        )
    }

    @Test
    fun `test use of unknown alias on SetConceptIdentifierValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.TextFacet::class) value: String
        )
    }

    @Test
    fun `test use of unknown alias on SetFacetValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @WithNewBuilder(builderClass = BuilderMethodWithUseOfUnknownAliasInLinkFacetAnnotation::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithUseOfUnknownAliasInLinkFacetAnnotation.() -> Unit,
        )


        @Builder
        @ExpectedAliasFromSuperiorBuilder("known")
        private interface BuilderMethodWithUseOfUnknownAliasInLinkFacetAnnotation {

            @Suppress("UNUSED")
            @BuilderMethod
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "unknown",
                facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.RefFacet::class,
                referencedConceptAlias = "known"
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the SetAliasConceptIdentifierReferenceFacetValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @WithNewBuilder(builderClass = BuilderMethodWithUseOfUnknownReferenceAliasInLinkFacetAnnotation::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithUseOfUnknownReferenceAliasInLinkFacetAnnotation.() -> Unit,
        )


        @Builder
        @ExpectedAliasFromSuperiorBuilder("known")
        private interface BuilderMethodWithUseOfUnknownReferenceAliasInLinkFacetAnnotation {

            @Suppress("UNUSED")
            @BuilderMethod
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "known",
                facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.RefFacet::class,
                referencedConceptAlias = "unknown"
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on SetAliasConceptIdentifierReferenceFacetValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.TextFacet::class, value = "some text")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringFacetValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedBooleanFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.BoolFacet::class, value = false)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedBooleanFacetValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedIntFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.NumberFacet::class, value = 42)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedIntFacetValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedEnumFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedEnumFacetValue(
            conceptToModifyAlias = "unknown",
            facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.EnumerationFacet::class,
            value = "A"
        )
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedEnumFacetValue annotation should throw an error`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFixedEnumFacetValueAnnotation::class) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @WithNewBuilder(builderClass = BuilderMethodUsingAnAliasFromParentBuilder::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodUsingAnAliasFromParentBuilder.() -> Unit,
        )


        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "known")
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "alsoKnown")
        private interface BuilderMethodUsingAnAliasFromParentBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomething(
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.TextFacet::class) value: String,
            )
        }
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should return without exceptions`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodCallingASubBuilderProvidingAnAlias::class) { builder ->
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @WithNewBuilder(builderClass = BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias.() -> Unit,
        )


        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "alsoKnown")
        private interface BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias {

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomething(
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.TextFacet::class) valueForKnown: String,
            )
        }
    }

    @Test
    fun `test omit alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertThrows(BuilderMethodSyntaxException::class.java) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias::class
                ) { builder ->
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @WithNewBuilder(builderClass = BuilderMethodReusingAnAliasNameFromSuperiorBuilder::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodReusingAnAliasNameFromSuperiorBuilder.() -> Unit,
        )


        @Builder
        @ExpectedAliasFromSuperiorBuilder(conceptAlias = "alsoKnown")
        private interface BuilderMethodReusingAnAliasNameFromSuperiorBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            @NewConcept(SchemaWithConceptWithTextFacet.ConceptWithFacet::class, declareConceptAlias = "known")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
            fun doSomething(
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = SchemaWithConceptWithTextFacet.ConceptWithFacet.TextFacet::class) valueForKnown: String,
            )
        }
    }

    @Test
    fun `test reuse an alias that is not expected from superior builder should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithTextFacet::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration::class
            ) { builder ->
                // do nothing
            }
        }
    }
}