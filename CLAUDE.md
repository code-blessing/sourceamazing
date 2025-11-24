# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SourceAmazing is a Kotlin framework for reading data from multiple sources (XML, Kotlin Builders, DSLs) into Java/Kotlin classes. The framework uses annotated interfaces to define schemas and automatically generates implementations, XSD schemas, and builder/DSL interfaces.

## Build Commands

### Core Build Tasks
- `./gradlew build` - Build all modules and run tests
- `./gradlew test` - Run all tests across all modules
- `./gradlew clean` - Clean build artifacts
- `./gradlew projects` - List all submodules

### Module-Specific Testing
- `./gradlew :sourceamazing-schema:test` - Run tests for a specific module
- `./gradlew test --fail-fast` - Stop on first test failure

### Code Formatting
- `./gradlew spotlessApply` - Format all Kotlin code with ktfmt (kotlinlangStyle)
- `./gradlew spotlessCheck` - Check if code is properly formatted

The project uses Spotless with ktfmt (kotlinlangStyle) for code formatting. All code changes must be formatted before committing.

## Architecture

### Module Structure

The project follows a strict API/implementation separation pattern:

#### Core Modules
- **sourceamazing-schema-api**: Public API for defining schemas using annotations (@Schema, @Concept, facet annotations, @Query* annotations)
- **sourceamazing-schema**: Implementation of the schema API; creates schema models and validates data

#### Builder Modules
- **sourceamazing-builder-api**: Public API for builder/DSL functionality (annotations: @Builder, @BuilderMethod, @NewConcept, @SetFacetValue, etc.)
- **sourceamazing-builder**: Implementation that interprets builder annotations and executes builder logic

#### XML Schema Modules
- **sourceamazing-xml-schema-api**: Public API for XML data import with auto-generated XSD schemas
- **sourceamazing-xml-schema**: Implementation that generates XSD and reads XML files

#### Support Modules
- **sourceamazing-utils**: Shared utility classes
- **sourceamazing-process-test**: End-to-end integration tests demonstrating complete workflows

### Key Architecture Patterns

#### Service Loader Pattern
The framework uses Java ServiceLoader for loose coupling between API and implementation modules:
- `SchemaApi.withSchema()` uses ServiceLoader to find `SchemaProcessorApi` implementations
- `BuilderApi.withBuilder()` uses ServiceLoader to find `BuilderProcessorApi` implementations

#### Annotation-Driven Design
The framework is heavily annotation-driven with two main annotation categories:
- **Schema Definitions**: @Schema, @Concept, @StringFacet, @IntFacet, @BooleanFacet, @EnumFacet, @ReferenceFacet define data structure
- **Data Queries**: @QueryConcepts, @QueryFacetValue, @QueryConceptIdentifierValue retrieve data from schema instances
- **Builder Directives**: @Builder, @BuilderMethod, @NewConcept, @SetFacetValue, @WithNewBuilder define builder behavior

#### Core Concepts

**Schema**: Root interface annotated with @Schema that declares concepts and provides query methods

**Concept**: Data entity interface annotated with @Concept that groups related facets (think: database table or object class)

**Facet**: Typed property/attribute of a concept defined by facet annotations (StringFacet, IntFacet, etc.)

**ConceptIdentifier**: Unique identifier for each concept instance (required for all concepts)

**SchemaContext**: Provides access to schema metadata (SchemaAccess) and data collection (ConceptDataCollector)

#### Data Flow
1. User defines schema interfaces with annotations
2. SchemaApi.withSchema() creates SchemaContext
3. Data is populated via XML (XmlSchemaApi) or Builders (BuilderApi)
4. Framework validates data against schema constraints
5. User retrieves data via @Query* annotated methods on schema proxy

## Development Guidelines

### Dependency Management
- Version catalog is defined in `settings.gradle.kts`
- Use `libs.plugins.kotlin.jvm` and `libs.kotlin.*` for dependencies
- Implementation modules must mark Kotlin stdlib/reflect as `compileOnly` to avoid transitive dependencies in published artifacts

### Testing
- All modules use JUnit 5
- Configure test tasks with `useJUnitPlatform()`
- Test fixtures are available in schema-api and utils modules for reuse

### Publishing
- Modules apply `sourceamazing-publishing` plugin from buildSrc
- Version is controlled via `gradle.properties` (currently 3.1.0)
- Published to Maven Central under group `org.codeblessing.sourceamazing`
