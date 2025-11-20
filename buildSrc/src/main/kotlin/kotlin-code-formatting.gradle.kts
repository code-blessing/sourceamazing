plugins {
    id("com.diffplug.spotless")
}
configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        // Format only real source files, avoid generated/build outputs
        target("src/**/*.kt")
        // Use ktfmt which keeps params on one line if they fit the max width, otherwise wraps
        // See: https://github.com/facebook/ktfmt and Spotless plugin docs
        ktfmt("0.52").googleStyle()
        // You may adjust maxWidth above if your team uses a different page size
    }
}
