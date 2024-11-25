plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

dependencies {
    testImplementation(libs.kotlin.test)
}
