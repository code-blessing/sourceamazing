plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
    `maven-dependency-repository`
    `java-test-fixtures`
}

dependencies {
    implementation(projects.sourceamazingSchemaApi)

    compileOnly(libs.kotlin.stdlib) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml
    compileOnly(libs.kotlin.reflect) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)

    testImplementation(libs.junit.jupiter)
    testFixturesImplementation(libs.junit.jupiter)
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