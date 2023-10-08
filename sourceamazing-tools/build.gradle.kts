plugins {
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
}


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
}



tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.codeblessing.sourceamazing"
            artifactId = "tools"
            version = "0.1"

            from(components["java"])

            pom {
                name.set("SourceAmazing Tools")
                description.set("A small libraries of tools working together with source amazing templates.")
                url.set("http://www.codeblessing.org")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                }
                developers {
                    developer {
                        name.set("Jonathan Weiss")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:code-blessing/sourceamazing.git")
                    url.set("https://github.com/code-blessing/sourceamazing")
                }

            }
        }
    }
}