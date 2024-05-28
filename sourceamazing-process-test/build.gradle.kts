plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(project(":sourceamazing-schema-api"))
    implementation(project(":sourceamazing-builder-api"))
    implementation(project(":sourceamazing-xml-schema-api"))
    runtimeOnly(project(":sourceamazing-schema"))
    runtimeOnly(project(":sourceamazing-builder"))
    runtimeOnly(project(":sourceamazing-xml-schema"))


    // to run an end-to-end test in junit, we need access to the sourceamazing-schema class
    // directly to bypass calling the main function
    testImplementation(project(":sourceamazing-schema"))
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

// can be run with gradle task "run"
application {
    mainClass.set("org.codeblessing.sourceamazing.processtest.ProcesstestKt")
}

// this demonstrates an alternative to the Gradle Application plugin
tasks.register<JavaExec>("runSourceAmazing") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.codeblessing.sourceamazing.processtest.ProcesstestKt")
}