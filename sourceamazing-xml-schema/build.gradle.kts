plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(projects.sourceamazingSchemaApi)
    implementation(projects.sourceamazingSchema)
    implementation(projects.sourceamazingXmlSchemaApi)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.hamcrest)
}



tasks.named<Test>("test") {
    useJUnitPlatform()
}


//
// Publishing
//
extensions.getByType<PublishingExtension>().publications {
    getByName<MavenPublication>("mavenSourceamazing") {
        artifactId = "sourceamazing-xml-schema"
        pom {
            name.set("SourceAmazing XML Schema")
            description.set("The implementation of the sourceamazing-xml-schema-api to add data to a sourceamazing model using XML files with automatically generated XSD schemas.")
        }
    }
}
