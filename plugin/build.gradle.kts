import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.PluginNotationSupplier

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
    alias(libs.plugins.jacoco.testkit)
    `jacoco-report-aggregation`
    `java-test-fixtures`
}

group = "io.github.gmazzo.test.aggregation"
description = "Test Aggregation Plugin for Android"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
testing.suites.create<JvmTestSuite>("kotlinTest")

gradlePlugin {
    website.set("https://github.com/gmazzo/gradle-android-test-aggregation-plugin")
    vcsUrl.set("https://github.com/gmazzo/gradle-android-test-aggregation-plugin")

    plugins.create("test-coverage-aggregation") {
        id = "io.github.gmazzo.test.aggregation.coverage"
        displayName = name
        implementationClass = "io.github.gmazzo.android.test.aggregation.TestCoverageAggregationPlugin"
        description = "Jacoco coverage aggregation support for Android/JVM modules"
        tags.addAll("android", "agp", "coverage", "jacoco", "test", "aggregation", "jacoco-report-aggregation")
    }

    plugins.create("test-results-aggregation") {
        id = "io.github.gmazzo.test.aggregation.results"
        displayName = name
        implementationClass = "io.github.gmazzo.android.test.aggregation.TestResultsAggregationPlugin"
        description = "Test results aggregation support for Android/JVM modules"
        tags.addAll("android", "agp", "test", "aggregation", "test-report-aggregation")
    }
}

samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

val pluginExtraClasspath by configurations.creating

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    fun DependencyHandler.plugin(dependency: PluginNotationSupplier) =
        plugin(dependency.asProvider())

    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.android))
    compileOnly(plugin(libs.plugins.kotlin.multiplatform))

    testFixturesApi(platform(libs.junit.bom))
    testFixturesApi(libs.junit.params)

    testFixturesImplementation(gradleKotlinDsl())
    testFixturesImplementation(gradleTestKit())
    testFixturesImplementation(plugin(libs.plugins.android))

    pluginExtraClasspath(plugin(libs.plugins.android))

    "kotlinTestImplementation"(testFixtures(project))
    "kotlinTestImplementation"(plugin(libs.plugins.kotlin.multiplatform))
}

testing.suites.withType<JvmTestSuite> {
    useJUnitJupiter()
}

tasks.withType<Test>().configureEach {
    val testFixtures by sourceSets

    testClassesDirs += testFixtures.output.classesDirs
    environment("TEMP_DIR", temporaryDir)
}

tasks.withType<JacocoReport>().configureEach {
    reports.xml.required = true
}

tasks.check {
    dependsOn(tasks.withType<JacocoReport>())
}

tasks.pluginUnderTestMetadata {
    pluginClasspath.from(pluginExtraClasspath)
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
