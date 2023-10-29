plugins {
    kotlin("jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codeblessing.sourceamazing:sourceamazing-api:1.1.0")
    runtimeOnly("org.codeblessing.sourceamazing:sourceamazing-engine:1.1.0")
    runtimeOnly("org.codeblessing.sourceamazing:sourceamazing-xml-schema:1.1.0")
}

application {
    mainClass.set("org.codeblessing.sourceamazing.engine.SourceamazingApplicationKt")
}
