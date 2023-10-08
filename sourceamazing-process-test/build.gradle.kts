plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(project(":sourceamazing-api"))
    implementation(project(":sourceamazing-tools"))
    runtimeOnly(project(":sourceamazing-engine"))
    runtimeOnly(project(":sourceamazing-xml-schema"))


    // to run an end-to-end test in junit, we need access to the engine directly to bypass calling the main function
    testImplementation(project(":sourceamazing-engine"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
    testImplementation("org.hamcrest:hamcrest:2.2")

}



tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named("run") {
    enabled = true
}

application {
    mainClass.set("org.codeblessing.sourceamazing.engine.SourceamazingApplicationKt")
}
