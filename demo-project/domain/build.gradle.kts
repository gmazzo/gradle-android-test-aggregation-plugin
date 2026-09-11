plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.github.gmazzo.test.aggregation")
    jacoco
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))

dependencies {
    testImplementation(libs.kotlin.test.junit)
}
