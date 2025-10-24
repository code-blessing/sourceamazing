import org.jetbrains.kotlin.config.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
    `kotlin-code-formatting`
}

repositories {
    mavenCentral()
}


dependencies {
    compileOnly(libs.kotlin.stdlib) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml
    compileOnly(libs.kotlin.reflect) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml

    implementation(projects.sourceamazingSchemaApi)
    implementation(projects.sourceamazingBuilderApi)
    implementation(projects.sourceamazingSchema)

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)

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


//
// Publishing
//
extensions.getByType<PublishingExtension>().publications {
    getByName<MavenPublication>("mavenSourceamazing") {
        artifactId = "sourceamazing-builder"
        pom {
            name.set("SourceAmazing Builder")
            description.set("The implementation of the sourceamazing-builder-api to add data to a sourceamazing model using builders or DSLs.")
        }
    }
}
