plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.9.0"
}

repositories {
    // The org.jetbrains.kotlin.jvm plugin requires a repository
    // where to download the Kotlin compiler dependencies from.
    mavenCentral()
}

dependencies {
    // Provides Spotless core + plugin implementation classes
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
}
