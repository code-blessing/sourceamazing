import org.gradle.api.plugins.internal.JavaPluginHelper
import org.gradle.internal.component.external.model.TestFixturesSupport
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent

plugins {
    alias(libs.plugins.kotlin.jvm)
    `sourceamazing-publishing`
    `maven-dependency-repository`
    `java-test-fixtures`
    `kotlin-code-formatting`
}

dependencies {
    implementation(projects.sourceamazingSchemaApi)

    compileOnly(libs.kotlin.stdlib) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml
    compileOnly(libs.kotlin.reflect) // add explicitly as "compileOnly" to remove kotlin dependency in pom.xml

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)

    testImplementation(libs.junit.jupiter)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

//
// Publishing
//
extensions.getByType<PublishingExtension>().publications {
    getByName<MavenPublication>("mavenSourceamazing") {
        artifactId = "sourceamazing-utils"
        pom {
            name.set("SourceAmazing Utils")
            description.set("Various Util classes")
        }
    }
}

plugins.withId("org.gradle.java-test-fixtures") {
    val component = JavaPluginHelper.getJavaComponent(project) as DefaultJvmSoftwareComponent
    val feature = component.features.getByName(TestFixturesSupport.TEST_FIXTURES_FEATURE_NAME)
    component.withVariantsFromConfiguration(feature.apiElementsConfiguration) { skip() }
    component.withVariantsFromConfiguration(feature.runtimeElementsConfiguration) { skip() }
}
