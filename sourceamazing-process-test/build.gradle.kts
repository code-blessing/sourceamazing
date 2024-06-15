plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(projects.sourceamazingSchemaApi)
    implementation(projects.sourceamazingBuilderApi)
    implementation(projects.sourceamazingXmlSchemaApi)
    runtimeOnly(projects.sourceamazingSchema)
    runtimeOnly(projects.sourceamazingBuilder)
    runtimeOnly(projects.sourceamazingXmlSchema)


    // to run an end-to-end test in junit, we need access to the sourceamazing-schema class
    // directly to bypass calling the main function
    testImplementation(projects.sourceamazingSchema)


    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.hamcrest)

}



tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named("run") {
    enabled = true
}

// can be run with gradle task "run"
application {
    mainClass.set("org.codeblessing.sourceamazing.processtest.ProcesstestKt")
}

// this demonstrates an alternative to the Gradle Application plugin
tasks.register<JavaExec>("runSourceAmazing") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.codeblessing.sourceamazing.processtest.ProcesstestKt")
}