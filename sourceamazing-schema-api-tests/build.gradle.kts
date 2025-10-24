import org.gradle.api.plugins.internal.JavaPluginHelper
import org.gradle.internal.component.external.model.TestFixturesSupport
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-dependency-repository`
    `kotlin-code-formatting`
    `java-test-fixtures`
}

dependencies {
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)
    testImplementation(projects.sourceamazingSchemaApi)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(projects.sourceamazingSchema)
    testFixturesImplementation(projects.sourceamazingSchemaApi)
    testFixturesImplementation(libs.junit.jupiter)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
