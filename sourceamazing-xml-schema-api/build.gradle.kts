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
        artifactId = "sourceamazing-xml-schema-api"
        pom {
            name.set("SourceAmazing XML Schema API")
            description.set("The API to add data to a sourceamazing model using XML files with automatically generated XSD schemas.")
        }
    }
}