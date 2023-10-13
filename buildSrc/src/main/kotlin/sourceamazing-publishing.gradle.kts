plugins {
    `java-library`
    `maven-publish`
    signing
}

val publicationName = "mavenSourceamazing"

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()

}

val publishingExtension: PublishingExtension = extensions.getByType<PublishingExtension>()

publishingExtension.repositories {
    maven {
        credentials {
            username =  project.properties.getOrDefault("sourceamazing.ossrhUsername", "<no username>") as String
            password =  project.properties.getOrDefault("sourceamazing.ossrhPassword", "<no password>") as String
        }

        // see https://central.sonatype.org/publish/publish-guide/#metadata-definition-and-upload
        val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
    }
}

publishingExtension.publications {
    create<MavenPublication>(publicationName) {
        groupId = "org.codeblessing.sourceamazing"
        version = project.property("sourceamazing.version") as String

        from(components["java"])

        pom {
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
                    email.set("jonathan.weiss@codeblessing.org")
                }
            }
            scm {
                connection.set("scm:git:git@github.com:code-blessing/sourceamazing.git")
                url.set("https://github.com/code-blessing/sourceamazing")
            }
        }
    }
}


configure<SigningExtension> {
    sign(publishingExtension.publications[publicationName])
}

tasks.getByName<Javadoc>("javadoc") {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}