plugins {
    kotlin("jvm")
    `sourceamazing-publishing`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":sourceamazing-schema-api"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
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