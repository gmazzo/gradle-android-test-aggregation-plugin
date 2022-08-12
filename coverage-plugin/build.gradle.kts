import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.PluginNotationSupplier

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(plugin(libs.plugins.android))
}

fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) = dependency.get().run {
    create("$pluginId:$pluginId.gradle.plugin:$version")
}

fun DependencyHandler.plugin(dependency: PluginNotationSupplier) =
    plugin(dependency.asProvider())
