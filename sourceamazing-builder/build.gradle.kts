plugins {
    kotlin("jvm")
    `sourceamazing-publishing`
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(project(":sourceamazing-schema-api"))
    implementation(project(":sourceamazing-builder-api"))
    implementation(project(":sourceamazing-schema"))


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
        artifactId = "sourceamazing-builder"
        pom {
            name.set("SourceAmazing Builder")
            description.set("The implementation of the sourceamazing-builder-api to add data to a sourceamazing model using builders or DSLs.")
        }
    }
}
