package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.BuilderApiAliasTest.MyConcepts.MyConcept
import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
class BuilderApiAliasTest {
    private interface MyConcepts {

        enum class MyEnumeration {
            A,
            B,
            C,
        }

        interface MyConcept {
            val text: String
            val bool: Boolean
            val number: Int
            val enumeration: MyEnumeration
            val reference: MyConcept
        }

        val concepts: List<MyConcept>
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForNewConcept {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for NewConcept annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateAliasForNewConcept::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @WithNewBuilder(builderClass = BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewConcept::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewConcept.() -> Unit
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "foo")
        private interface BuilderMethodWithDuplicateAliasImportedFromSuperiorForNewConcept {

            @BuilderMethod
            @NewConcept(MyConcept::class, declareConceptAlias = "foo")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething()
        }
    }

    @Test
    fun `test duplicate alias from superior concept for NewConcept annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithNewConcept {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProviderWithDuplicateAlias)

        @BuilderDataProvider
        class DataProviderWithDuplicateAlias {

            @BuilderData
            @NewConcept(MyConcept::class, declareConceptAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test duplicate alias concept from builder method and data provider for NewConcept annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithNewConcept::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForRandomConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for SetRandomConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithDuplicateAliasForRandomConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetRandomId {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProviderWithDuplicateAlias)

        @BuilderDataProvider
        class DataProviderWithDuplicateAlias {

            @BuilderData
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test duplicate alias with Builder and Data Provider for SetRandomConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetRandomId::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier2: ConceptIdentifier,
        )
    }

    @Test
    fun `test duplicate alias for ConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_SET_CONCEPT_IDENTIFIER_VALUE_USAGE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetId {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @ProvideBuilderData data: DataProviderWithDuplicateAlias,
        )

        @BuilderDataProvider
        class DataProviderWithDuplicateAlias {

            @BuilderData
            @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun getConceptId(): ConceptIdentifier {
                throw NotImplementedError("Method is not called in validation phase")
            }
        }
    }

    @Test
    fun `test duplicate alias with Builder and Data Provider for ConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_SET_CONCEPT_IDENTIFIER_VALUE_USAGE,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetId::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutConceptIdentifierForAlias {

        @BuilderMethod @NewConcept(MyConcept::class, declareConceptAlias = "foo") fun doSomething()
    }

    @Test
    fun `test missing concept identifier declaration for alias should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutConceptIdentifierForAlias::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutConceptIdentifierForAliasInDataProvider {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @ProvideBuilderData data: DataProviderWithConceptWithoutConceptId,
        )

        @BuilderDataProvider
        class DataProviderWithConceptWithoutConceptId {

            @BuilderData
            @NewConcept(MyConcept::class, declareConceptAlias = "BAR")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test missing concept identifier declaration in data provider for alias should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithoutConceptIdentifierForAliasInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithDuplicateMixedConceptIdentifier {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier: ConceptIdentifier)
    }

    @Test
    fun `test duplicate alias with SetRandomConceptIdentifierValue and SetConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_CONCEPT_IDENTIFIER_INITIALIZATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithDuplicateMixedConceptIdentifier::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithRandomIdAndConceptIdentifierForAliasInDataProvider {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProviderWithConceptWithoutConceptId)

        @BuilderDataProvider
        class DataProviderWithConceptWithoutConceptId {

            @BuilderData
            @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun getConceptId(): ConceptIdentifier {
                throw NotImplementedError("Method is not called in validation phase")
            }
        }
    }

    @Test
    fun `test duplicate alias with SetRandomConceptIdentifierValue in builder and SetConceptIdentifierValue in data provider annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_CONCEPT_IDENTIFIER_INITIALIZATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithRandomIdAndConceptIdentifierForAliasInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutAssignmentOfConceptIdentifier {

        @BuilderMethod @NewConcept(MyConcept::class, declareConceptAlias = "foo") fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "foo")
        private interface NestedBuilder {

            @BuilderMethod @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo") fun doSomethingNested()
        }
    }

    @Test
    fun `test new concept with assignment of concept identifier but in the nested builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(schemaContext, BuilderMethodWithoutAssignmentOfConceptIdentifier::class) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithoutAssignmentOfConceptIdentifierInBuilderButInDataProvider {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        fun doSomething(@ProvideBuilderData data: DataProviderWithConceptId)

        @BuilderDataProvider
        class DataProviderWithConceptId {

            @BuilderData
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test new concept with assignment of concept identifier but in the data provider should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodWithoutAssignmentOfConceptIdentifierInBuilderButInDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifier {
        @BuilderMethod @SetRandomConceptIdentifierValue(conceptToModifyAlias = "unknown") fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetRandomConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifierInDataProvider {

        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test use of unknown alias on SetRandomConceptIdentifierValue annotation in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifierInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotation {
        @BuilderMethod
        fun doSomething(@SetConceptIdentifierValue(conceptToModifyAlias = "unknown") id: ConceptIdentifier)
    }

    @Test
    fun `test use of unknown alias on SetConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotationInDataProvider {

        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun getConceptId(): ConceptIdentifier {
                throw NotImplementedError("Method is not called in validation phase")
            }
        }
    }

    @Test
    fun `test use of unknown alias on SetProvidedConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotationInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotation {

        @BuilderMethod
        fun doSomething(@SetFacetValue(conceptToModifyAlias = "unknown", facetToModify = "text") value: String)
    }

    @Test
    fun `test use of unknown alias on SetFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotationInDataProvider {

        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetProvidedFacetValue(conceptToModifyAlias = "unknown", facetToModify = "text")
            fun getFacetValue(): String {
                throw NotImplementedError("Method is not called in validation phase")
            }
        }
    }

    @Test
    fun `test use of unknown alias on SetProvidedFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotationInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @WithNewBuilder(builderClass = BuilderMethodWithUseOfUnknownAliasInLinkFacetAnnotation::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithUseOfUnknownAliasInLinkFacetAnnotation.() -> Unit
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "known")
        private interface BuilderMethodWithUseOfUnknownAliasInLinkFacetAnnotation {

            @BuilderMethod
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "unknown",
                facetToModify = "RefFacet",
                referencedConceptAlias = "known",
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the SetAliasConceptIdentifierReferenceFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndDataProviderHavingUnknownAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "unknown",
                facetToModify = "RefFacet",
                referencedConceptAlias = "known",
            )
            fun getFacetValue(): String {
                throw NotImplementedError("Method is not called in validation phase")
            }
        }
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the SetAliasConceptIdentifierReferenceFacetValue annotation in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndDataProviderHavingUnknownAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @WithNewBuilder(builderClass = BuilderMethodWithUseOfUnknownReferenceAliasInLinkFacetAnnotation::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodWithUseOfUnknownReferenceAliasInLinkFacetAnnotation.() -> Unit
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "known")
        private interface BuilderMethodWithUseOfUnknownReferenceAliasInLinkFacetAnnotation {

            @BuilderMethod
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "known",
                facetToModify = "RefFacet",
                referencedConceptAlias = "unknown",
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on SetAliasConceptIdentifierReferenceFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithAliasAndDataProviderHavingUnknownReferenceAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetAliasConceptIdentifierReferenceFacetValue(
                conceptToModifyAlias = "known",
                facetToModify = "RefFacet",
                referencedConceptAlias = "unknown",
            )
            fun getFacetValue(): String {
                throw NotImplementedError("Method is not called in validation phase")
            }
        }
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on SetAliasConceptIdentifierReferenceFacetValue annotation in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithAliasAndDataProviderHavingUnknownReferenceAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotation {
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = "text", value = "some text")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotationInDataProvider {
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = "text", value = "some text")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringFacetValue annotation in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotationInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation {
        @BuilderMethod fun doSomething(@ProvideBuilderData data: DataProvider)

        @BuilderDataProvider
        class DataProvider {

            @BuilderData
            @SetFixedBooleanFacetValue(conceptToModifyAlias = "unknown", facetToModify = "BoolFacet", value = false)
            fun getFacetValue(): String {
                throw NotImplementedError("Method is not called in validation phase")
            }
        }
    }

    @Test
    fun `test use of unknown alias on SetFixedBooleanFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation {
        @BuilderMethod
        @SetFixedIntFacetValue(conceptToModifyAlias = "unknown", facetToModify = "NumberFacet", value = 42)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedIntFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodWithUseOfUnknownAliasInFixedEnumFacetValueAnnotation {
        @BuilderMethod
        @SetFixedEnumFacetValue(conceptToModifyAlias = "unknown", facetToModify = "EnumerationFacet", value = "A")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedEnumFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodWithUseOfUnknownAliasInFixedEnumFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(MyConcept::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @WithNewBuilder(builderClass = BuilderMethodUsingAnAliasFromParentBuilder::class)
        fun doInjectASubBuilder(@InjectBuilder builder: BuilderMethodUsingAnAliasFromParentBuilder.() -> Unit)

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "known")
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "alsoKnown")
        private interface BuilderMethodUsingAnAliasFromParentBuilder {

            @BuilderMethod
            fun doSomething(@SetFacetValue(conceptToModifyAlias = "known", facetToModify = "text") value: String)
        }
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(schemaContext, BuilderMethodCallingASubBuilderProvidingAnAlias::class) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(MyConcept::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @WithNewBuilder(builderClass = BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias::class)
        fun doInjectASubBuilder(
            @InjectBuilder builder: BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias.() -> Unit
        )

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "alsoKnown")
        private interface BuilderMethodUsingAnAliasFromParentBuilderWithoutExpectAlias {

            @BuilderMethod
            fun doSomething(
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = "text") valueForKnown: String
            )
        }
    }

    @Test
    fun `test omit alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @NewConcept(MyConcept::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @WithNewBuilder(builderClass = BuilderMethodReusingAnAliasNameFromSuperiorBuilder::class)
        fun doInjectASubBuilder(@InjectBuilder builder: BuilderMethodReusingAnAliasNameFromSuperiorBuilder.() -> Unit)

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "alsoKnown")
        private interface BuilderMethodReusingAnAliasNameFromSuperiorBuilder {

            @BuilderMethod
            @NewConcept(MyConcept::class, declareConceptAlias = "known")
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
            fun doSomething(
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = "text") valueForKnown: String
            )
        }
    }

    @Test
    fun `test reuse an alias that is not expected from superior builder should not fail`() {
        SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
            withRootInstance<MyConcepts>(schemaContext) {
                BuilderApi.withBuilder(
                    schemaContext,
                    BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    private interface BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation {

        @BuilderMethod
        @NewConcept(MyConcept::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(): NestedBuilder

        @Builder
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "foo")
        @ExpectedAliasFromSuperiorBuilder(concept = MyConcept::class, conceptAlias = "foo")
        private interface NestedBuilder {

            @BuilderMethod fun doSomethingNested()
        }
    }

    @Test
    fun `test duplicate alias in ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION,
        ) {
            SchemaApi.withSchema(MyConcepts::class) { schemaContext ->
                withRootInstance<MyConcepts>(schemaContext) {
                    BuilderApi.withBuilder(
                        schemaContext,
                        BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
