plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.android.lib) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
}

val pluginBuild = gradle.includedBuild("plugin")

tasks.register(LifecycleBasePlugin.BUILD_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}

tasks.register(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME) {
    dependsOn(pluginBuild.task(":$name"))
}
