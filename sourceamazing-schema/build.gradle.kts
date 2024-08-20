plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-test-fixtures`
    `sourceamazing-publishing`
    `maven-dependency-repository`
}

dependencies {
    implementation(projects.sourceamazingSchemaApi)

    implementation(libs.kotlin.reflect)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

//
// Publishing
//
extensions.getByType<PublishingExtension>().publications {
    getByName<MavenPublication>("mavenSourceamazing") {
        artifactId = "sourceamazing-schema"
        pom {
            name.set("SourceAmazing Schema")
            description.set("The implementation of the sourceamazing-schema-api to create a sourceamazing model based on a schema interface")
        }
    }
}