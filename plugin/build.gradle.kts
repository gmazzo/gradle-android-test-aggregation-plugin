import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.PluginNotationSupplier

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.pluginPublish)
}

description = "Test Aggregation Plugin for Android"

gradlePlugin {
    website.set("https://github.com/gmazzo/gradle-android-test-aggregation-plugin")
    vcsUrl.set("https://github.com/gmazzo/gradle-android-test-aggregation-plugin")

    plugins.create("test-aggregation") {
        id = "io.github.gmazzo.test-aggregation"
        displayName = name
        implementationClass = "io.github.gmazzo.android.test.aggregation.TestAggregationPlugin"
        description = "Jacoco and Test results aggregation support for Android modules"
        tags.addAll("android", "agp", "coverage", "jacoco", "test", "aggregation", "jacoco-report-aggregation", "test-report-aggregation")
    }
}

samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    fun DependencyHandler.plugin(dependency: PluginNotationSupplier) =
        plugin(dependency.asProvider())

    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.android))
}
