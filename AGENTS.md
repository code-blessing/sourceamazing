# Repository Guidelines

## Project Structure & Module Organization
- Kotlin multi-module Gradle build; all sources live in `src/main/kotlin`, tests in `src/test/kotlin`, and resources alongside each module.
- Key modules: `sourceamazing-schema-api` (public annotations/contracts), `sourceamazing-schema` (schema implementation/validation), `sourceamazing-builder-api` and `sourceamazing-builder` (builder/DSL generation), `sourceamazing-xml-schema-api` and `sourceamazing-xml-schema` (XML schema + parser), `sourceamazing-utils` (shared helpers), `sourceamazing-process-test` (end-to-end process checks).
- Use module-qualified paths (e.g., `sourceamazing-schema/src/main/kotlin/...`) when adding new files so types stay colocated with their domain.

## Build, Test, and Development Commands
- `./gradlew build` — full compile, formatting check, and tests for every module.
- `./gradlew test` or `./gradlew :module:test` — run JUnit 5 suites globally or for a specific module (e.g., `:sourceamazing-builder:test`).
- `./gradlew spotlessApply` — auto-format Kotlin via ktfmt; run before commits when code changes.
- `./gradlew spotlessCheck` — verify formatting in CI style without modifying files.

## Coding Style & Naming Conventions
- Kotlin code follows ktfmt Kotlinlang style (via Spotless); no manual reflow needed. Two-space indent output; imports and wrapping are tool-managed.
- Package names stay lowercase dot-separated under `org.codeblessing.sourceamazing.*`. Public API types match existing noun-based naming (SchemaApi, BuilderApi, XmlSchemaApi).
- Tests end with `Test` (see `AnnotationExtensionsTest.kt`); mirror package of the code under test.

## Testing Guidelines
- JUnit Jupiter is standard; prefer clear, isolated unit tests in `src/test/kotlin` and use fixtures from the same module when available.
- Add coverage for new branches/edge cases (nullability, references, identifier generation, builder/DSL flows). Keep smoke/integration checks in `sourceamazing-process-test` or module-level smoke tests.
- Run `./gradlew test` locally before pushing; include data samples/resources under the module’s `src/test/resources` when needed.

## Commit & Pull Request Guidelines
- Commit messages are short, imperative, and scoped (e.g., “Move @Suppress annotation to interfaces”, “Format with spotless”). Keep related changes together.
- PRs should describe the change, affected modules, and rationale; link issues/tickets when applicable.
- Note test coverage in the PR description (`./gradlew test` run, modules touched) and call out any schema/builder/DSL behavior changes or XML format impacts.
- Ensure formatting (`spotlessApply`) and tests are clean before requesting review; include small code/context snippets or screenshots when behavior/output changes.
