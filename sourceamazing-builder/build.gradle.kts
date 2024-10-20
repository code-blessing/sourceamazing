plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(projects.sourceamazingSchemaApi)
    implementation(projects.sourceamazingBuilderApi)
    implementation(projects.sourceamazingSchema)

    compileOnly(libs.kotlin.stdlib) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml
    compileOnly(libs.kotlin.reflect) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)

    testImplementation(libs.junit.jupiter)
    testImplementation(testFixtures(projects.sourceamazingSchema))
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
