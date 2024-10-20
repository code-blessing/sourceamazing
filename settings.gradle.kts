rootProject.name = "sourceamazing"

// see https://docs.gradle.org/current/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlinVersion = version("kotlin", "1.9.24")
            val junit5Version = version("junit5", "5.11.0")

            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef(kotlinVersion)
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef(junit5Version)
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef(kotlinVersion)

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlinVersion)
        }
    }
}

include("sourceamazing-schema-api")
include("sourceamazing-schema")
include("sourceamazing-builder-api")
include("sourceamazing-builder")
include("sourceamazing-xml-schema-api")
include("sourceamazing-xml-schema")
include("sourceamazing-process-test")
