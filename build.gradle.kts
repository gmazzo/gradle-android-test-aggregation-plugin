plugins {
    base
    `maven-publish`
    alias(libs.plugins.android) apply false
    alias(libs.plugins.android.lib) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.publicationsReport)
}

val pluginBuild = gradle.includedBuild("plugin")

tasks.build {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.check {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.publish {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.publishToMavenLocal {
    dependsOn(pluginBuild.task(":$name"))
}
