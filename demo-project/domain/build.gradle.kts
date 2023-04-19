plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
}

dependencies {
    testImplementation(libs.junit)
}
