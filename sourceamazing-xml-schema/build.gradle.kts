plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
    `maven-dependency-repository`
}

dependencies {
    implementation(projects.sourceamazingSchemaApi)
    implementation(projects.sourceamazingSchema)
    implementation(projects.sourceamazingXmlSchemaApi)

    testImplementation(libs.junit.jupiter)
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
