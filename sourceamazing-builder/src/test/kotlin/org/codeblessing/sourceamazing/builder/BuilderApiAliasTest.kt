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
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodParameterSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.BooleanFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Concept
import org.codeblessing.sourceamazing.schema.api.annotations.EnumFacet
import org.codeblessing.sourceamazing.schema.api.annotations.IntFacet
import org.codeblessing.sourceamazing.schema.api.annotations.ReferenceFacet
import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.codeblessing.sourceamazing.schema.api.annotations.StringFacet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.junit.jupiter.api.Test

class BuilderApiAliasTest {
    @Schema(concepts = [SchemaWithConceptWithFacet.ConceptWithFacet::class])
    private interface SchemaWithConceptWithFacet {

        enum class MyEnumeration {
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
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for NewConcept annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForNewConcept::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
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
            @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething()
        }
    }

    @Test
    fun `test duplicate alias from superior concept for NewConcept annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForRandomConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for SetRandomConceptIdentifierValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForRandomConceptIdentifier::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier2: ConceptIdentifier,
        )
    }

    @Test
    fun `test duplicate alias for ConceptIdentifierValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.DUPLICATE_SET_CONCEPT_IDENTIFIER_VALUE_USAGE) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutConceptIdentifierForAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test missing concept identifier declaration for alias should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutConceptIdentifierForAlias::class) { 
                    // do nothing
                }
            }
        }
    }


    @Builder
    private interface BuilderMethodWithDuplicateMixedConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier: ConceptIdentifier
        )
    }

    @Test
    fun `test duplicate alias with SetRandomConceptIdentifierValue and SetConceptIdentifierValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.DUPLICATE_CONCEPT_IDENTIFIER_INITIALIZATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateMixedConceptIdentifier::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutAssignmentOfConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomethingNested()
        }
    }

    @Test
    fun `test new concept with assignment of concept identifier but in the nested builder should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutAssignmentOfConceptIdentifier::class) { 
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
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifier::class) { 
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
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotation::class) { 
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
            @SetFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class) value: String
        )
    }

    @Test
    fun `test use of unknown alias on SetFacetValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodParameterSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
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
                facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.RefFacet::class,
                referencedConceptAlias = "known"
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the SetAliasConceptIdentifierReferenceFacetValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
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
                facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.RefFacet::class,
                referencedConceptAlias = "unknown"
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on SetAliasConceptIdentifierReferenceFacetValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class, value = "some text")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringFacetValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInDefaultStringFacetValueAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedBooleanFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.BoolFacet::class, value = false)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedBooleanFacetValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedIntFacetValue(conceptToModifyAlias = "unknown", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.NumberFacet::class, value = 42)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedIntFacetValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation::class) { 
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
            facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.EnumerationFacet::class,
            value = "A"
        )
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedEnumFacetValue annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderMethodWithUseOfUnknownAliasInFixedEnumFacetValueAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
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
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class) value: String,
            )
        }
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(schemaContext, BuilderMethodCallingASubBuilderProvidingAnAlias::class) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
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
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class) valueForKnown: String,
            )
        }
    }

    @Test
    fun `test omit alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodParameterSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias::class
                ) { 
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
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
            @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
            fun doSomething(
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = SchemaWithConceptWithFacet.ConceptWithFacet.TextFacet::class) valueForKnown: String,
            )
        }
    }

    @Test
    fun `test reuse an alias that is not expected from superior builder should not fail`() {
        SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
            BuilderApi.withBuilder(
                schemaContext,
                BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration::class
            ) { 
                // do nothing
            }
        }
    }

    @Builder
    private interface BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder("foo")
        @ExpectedAliasFromSuperiorBuilder("foo")
        private interface NestedBuilder {

            @Suppress("UNUSED")
            @BuilderMethod
            fun doSomethingNested()
        }
    }

    @Test
    fun `test duplicate alias in ExpectedAliasFromSuperiorBuilder annotation should throw an error`() {
        assertExceptionWithErrorCode(BuilderSyntaxException::class, BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION) {
            SchemaApi.withSchema(schemaDefinitionClass = SchemaWithConceptWithFacet::class) { schemaContext ->
                BuilderApi.withBuilder(schemaContext, BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation::class) { 
                    // do nothing
                }
            }
        }
    }

}