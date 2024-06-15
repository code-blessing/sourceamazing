rootProject.name = "sourceamazing"

// see https://docs.gradle.org/current/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val junit5Version = version("junit5", "5.9.1")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef(junit5Version)

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version("1.9.24")
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
