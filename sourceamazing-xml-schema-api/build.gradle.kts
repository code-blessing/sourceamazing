plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
    `maven-dependency-repository`
}

dependencies {
    implementation(projects.sourceamazingSchemaApi)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}


//
// Publishing
//
extensions.getByType<PublishingExtension>().publications {
    getByName<MavenPublication>("mavenSourceamazing") {
        artifactId = "sourceamazing-xml-schema-api"
        pom {
            name.set("SourceAmazing XML Schema API")
            description.set("The API to add data to a sourceamazing model using XML files with automatically generated XSD schemas.")
        }
    }
}