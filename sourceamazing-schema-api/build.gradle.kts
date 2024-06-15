plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
}

repositories {
    mavenCentral()
}

dependencies {
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
        artifactId = "sourceamazing-schema-api"
        pom {
            name.set("SourceAmazing Schema API")
            description.set("The API to create a sourceamazing model based on a schema interface.")
        }
    }
}