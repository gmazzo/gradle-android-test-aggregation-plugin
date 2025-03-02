plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))

dependencies {
    testImplementation(libs.kotlin.test)
}
