package org.codeblessing.sourceamazing.builder

import org.codeblessing.sourceamazing.builder.api.BuilderApi
import org.codeblessing.sourceamazing.builder.api.annotations.*
import org.codeblessing.sourceamazing.builder.exceptions.BuilderMethodSyntaxException
import org.codeblessing.sourceamazing.builder.exceptions.BuilderSyntaxException
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.SchemaApi
import org.codeblessing.sourceamazing.schema.api.annotations.Facet
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.withRootInstance
import org.codeblessing.sourceamazing.toConceptName
import org.junit.jupiter.api.Test

class BuilderApiAliasTest {
    private interface SchemaWithConceptWithFacet {

        enum class MyEnumeration {
            @Suppress("UNUSED")
            A,
            @Suppress("UNUSED")
            B,
            @Suppress("UNUSED")
            C,
        }

        interface ConceptWithFacet {
            @Suppress("UNUSED")
            @Facet
            val text: String

            @Suppress("UNUSED")
            @Facet
            val bool: Boolean

            @Suppress("UNUSED")
            @Facet
            val number: Int

            @Suppress("UNUSED")
            @Facet
            val enumeration: MyEnumeration

            @Suppress("UNUSED")
            @Facet
            val reference: ConceptWithFacet
        }

        @Suppress("UNUSED")
        @Facet
        val concepts: List<ConceptWithFacet>

    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithDuplicateAliasForNewConcept {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for NewConcept annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithDuplicateAliasForNewConcept::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
    fun `test duplicate alias from superior concept for NewConcept annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndSubBuilderHavingDuplicatedAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithNewConcept {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProviderWithDuplicateAlias,
        )


        @BuilderDataProvider
        class DataProviderWithDuplicateAlias {

            @Suppress("UNUSED")
            @BuilderData
            @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test duplicate alias concept from builder method and data provider for NewConcept annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.ALIAS_IS_ALREADY_USED) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithNewConcept::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithDuplicateAliasForRandomConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test duplicate alias for SetRandomConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_SET_RANDOM_CONCEPT_IDENTIFIER_VALUE_USAGE,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithDuplicateAliasForRandomConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetRandomId {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProviderWithDuplicateAlias,
        )


        @BuilderDataProvider
        class DataProviderWithDuplicateAlias {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetRandomId::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithDuplicateAliasForManuallySetConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetId {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @ProvideBuilderData data: DataProviderWithDuplicateAlias,
        )


        @BuilderDataProvider
        class DataProviderWithDuplicateAlias {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndDataProviderHavingDuplicatedAliasWithSetId::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithoutConceptIdentifierForAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething()
    }

    @Test
    fun `test missing concept identifier declaration for alias should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithoutConceptIdentifierForAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithoutConceptIdentifierForAliasInDataProvider {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier1: ConceptIdentifier,
            @ProvideBuilderData data: DataProviderWithConceptWithoutConceptId,
        )


        @BuilderDataProvider
        class DataProviderWithConceptWithoutConceptId {

            @Suppress("UNUSED")
            @BuilderData
            @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "BAR")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithoutConceptIdentifierForAliasInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithDuplicateMixedConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "foo") conceptIdentifier: ConceptIdentifier
        )
    }

    @Test
    fun `test duplicate alias with SetRandomConceptIdentifierValue and SetConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.DUPLICATE_CONCEPT_IDENTIFIER_INITIALIZATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithDuplicateMixedConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithRandomIdAndConceptIdentifierForAliasInDataProvider {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProviderWithConceptWithoutConceptId,
        )


        @BuilderDataProvider
        class DataProviderWithConceptWithoutConceptId {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithRandomIdAndConceptIdentifierForAliasInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithoutAssignmentOfConceptIdentifier {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
    fun `test new concept with assignment of concept identifier but in the nested builder should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderMethodSyntaxException::class,
            BuilderErrorCode.CONCEPT_HAS_NO_CORRESPONDING_CONCEPT_IDENTIFIER,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithoutAssignmentOfConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithoutAssignmentOfConceptIdentifierInBuilderButInDataProvider {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
        fun doSomething(
            @ProvideBuilderData data: DataProviderWithConceptId
        )

        @BuilderDataProvider
        class DataProviderWithConceptId {

            @Suppress("UNUSED")
            @BuilderData
            @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
            fun doSomething() {
                // nothing to do
            }
        }
    }

    @Test
    fun `test new concept with assignment of concept identifier but in the data provider should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodWithoutAssignmentOfConceptIdentifierInBuilderButInDataProvider::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifier {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "unknown")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetRandomConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifier::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifierInDataProvider {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )


        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInAutoRandomConceptIdentifierInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @SetConceptIdentifierValue(conceptToModifyAlias = "unknown") id: ConceptIdentifier
        )
    }

    @Test
    fun `test use of unknown alias on SetConceptIdentifierValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotationInDataProvider {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )


        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInConceptIdentifierValueAnnotationInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @SetFacetValue(conceptToModifyAlias = "unknown", facetToModify = "text") value: String
        )
    }

    @Test
    fun `test use of unknown alias on SetFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotationInDataProvider {

        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )


        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInFacetValueAnnotationInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "known")
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
                facetToModify = "RefFacet",
                referencedConceptAlias = "known",
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property conceptToModifyAlias on the SetAliasConceptIdentifierReferenceFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndSubBuilderHavingUnknownAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndDataProviderHavingUnknownAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "known")
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )


        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndDataProviderHavingUnknownAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "known")
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
                facetToModify = "RefFacet",
                referencedConceptAlias = "unknown",
            )
            fun doSomething()
        }
    }

    @Test
    fun `test use of unknown alias in property referencedConceptAlias on SetAliasConceptIdentifierReferenceFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndSubBuilderHavingUnknownReferenceAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithAliasAndDataProviderHavingUnknownReferenceAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "known")
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithAliasAndDataProviderHavingUnknownReferenceAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }


    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = "text", value = "some text")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotationInDataProvider {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedStringFacetValue(conceptToModifyAlias = "unknown", facetToModify = "text", value = "some text")
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedStringFacetValue annotation in data provider should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInFixedStringFacetValueAnnotationInDataProvider::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        fun doSomething(
            @ProvideBuilderData data: DataProvider
        )

        @BuilderDataProvider
        class DataProvider {

            @Suppress("UNUSED")
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
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInFixedBooleanFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedIntFacetValue(conceptToModifyAlias = "unknown", facetToModify = "NumberFacet", value = 42)
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedIntFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInFixedIntegerFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodWithUseOfUnknownAliasInFixedEnumFacetValueAnnotation {
        @Suppress("UNUSED")
        @BuilderMethod
        @SetFixedEnumFacetValue(
            conceptToModifyAlias = "unknown",
            facetToModify = "EnumerationFacet",
            value = "A",
        )
        fun doSomething()
    }

    @Test
    fun `test use of unknown alias on SetFixedEnumFacetValue annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodWithUseOfUnknownAliasInFixedEnumFacetValueAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodCallingASubBuilderProvidingAnAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "known")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "alsoKnown")
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
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = "text") value: String,
            )
        }
    }

    @Test
    fun `test use of alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodCallingASubBuilderProvidingAnAlias::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "known")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "alsoKnown")
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
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = "text") valueForKnown: String,
            )
        }
    }

    @Test
    fun `test omit alias expectation from calling builder with ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(BuilderMethodSyntaxException::class, BuilderErrorCode.UNKNOWN_ALIAS) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderMethodCallingASubBuilderProvidingAnAliasWithoutExpectAlias::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "known")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "known")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "known")
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "alsoKnown")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "alsoKnown")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "alsoKnown")
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
                @SetFacetValue(conceptToModifyAlias = "known", facetToModify = "text") valueForKnown: String,
            )
        }
    }

    @Test
    fun `test reuse an alias that is not expected from superior builder should not fail`() {
        SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
            withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                BuilderApi.withBuilder(
                    schemaContext,
                    schemaContext.toConceptName(rootConceptIdentifier),
                    rootConceptIdentifier,
                    BuilderMethodCallingASubBuilderProvidingAnAliasWithRedeclaration::class,
                ) {
                    // do nothing
                }
            }
        }
    }

    @Builder
    @ExpectedRootAlias("root")
    private interface BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation {

        @Suppress("UNUSED")
        @BuilderMethod
        @NewConcept(SchemaWithConceptWithFacet.ConceptWithFacet::class, declareConceptAlias = "foo")
        @SetRandomConceptIdentifierValue(conceptToModifyAlias = "foo")
        @SetAliasConceptIdentifierReferenceFacetValue(conceptToModifyAlias = "root", facetToModify = "concepts", referencedConceptAlias = "foo")
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
    fun `test duplicate alias in ExpectedAliasFromSuperiorBuilder annotation should throw an exception`() {
        assertExceptionWithErrorCode(
            BuilderSyntaxException::class,
            BuilderErrorCode.DUPLICATE_ALIAS_IN_EXPECTED_ALIAS_FROM_SUPERIOR_BUILDER_ANNOTATION,
        ) {
            SchemaApi.withSchema(SchemaWithConceptWithFacet::class) { schemaContext ->
                withRootInstance<SchemaWithConceptWithFacet>(schemaContext) { rootConceptIdentifier ->
                    BuilderApi.withBuilder(
                        schemaContext,
                        schemaContext.toConceptName(rootConceptIdentifier),
                        rootConceptIdentifier,
                        BuilderWithDuplicateAliasesInExpectedAliasFromSuperiorBuilderAnnotation::class,
                    ) {
                        // do nothing
                    }
                }
            }
        }
    }
}
