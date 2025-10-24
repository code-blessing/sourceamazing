import org.jetbrains.kotlin.config.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-code-formatting`
}

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)
    testImplementation(projects.sourceamazingSchemaApi)
    testImplementation(projects.sourceamazingBuilderApi)
    testRuntimeOnly(projects.sourceamazingSchema)
    testRuntimeOnly(projects.sourceamazingBuilder)

    testImplementation(libs.junit.jupiter)
    testImplementation(testFixtures(projects.sourceamazingSchemaApiTests))
}

kotlin {
    compilerOptions {
        // do use java default methods on interfaces
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}


tasks.named<Test>("test") {
    useJUnitPlatform()
}

