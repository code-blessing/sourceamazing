plugins {
    kotlin("jvm")
    `sourceamazing-publishing`
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(project(":sourceamazing-schema-api"))
    implementation(project(":sourceamazing-schema"))
    implementation(project(":sourceamazing-xml-schema-api"))


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
            name.set("SourceAmazing XML Schema")
            description.set("The implementation of the sourceamazing-xml-schema-api to add data to a sourceamazing model using XML files with automatically generated XSD schemas.")
        }
    }
}
