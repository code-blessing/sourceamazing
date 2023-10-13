plugins {
    kotlin("jvm")
    `sourceamazing-publishing`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":sourceamazing-api"))
    implementation(project(":sourceamazing-tools"))


    testImplementation(project(":sourceamazing-engine"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
    testImplementation("org.hamcrest:hamcrest:2.2")

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
            name.set("SourceAmazing XML Schemas")
            description.set("Creates out of the SourceAmazing schema a XML Schema to maintain the input data.")
        }
    }
}
