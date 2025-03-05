import com.android.build.api.AndroidPluginVersion
import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.PluginNotationSupplier

plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.multiapi)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.gradle.testkit.jacoco)
    alias(libs.plugins.publicationsReport)
    `jacoco-report-aggregation`
    `java-test-fixtures`
}

group = "io.github.gmazzo.test.aggregation"
description = "Test Aggregation Plugin for Android"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)
testing.suites.create<JvmTestSuite>("kotlinTest")

val minGradleVersion = "8.0"
val minAGPVersion = "8.1.0"

buildConfig {
    packageName = "io.github.gmazzo.android.test.aggregation"
    buildConfigField<GradleVersion>(
        "MIN_GRADLE_VERSION",
        expression("GradleVersion.version(\"$minGradleVersion\")")
    )
    buildConfigField<AndroidPluginVersion>(
        "MIN_AGP_VERSION",
        expression("AndroidPluginVersion(${minAGPVersion.replace('.', ',')})")
    )
}

gradlePlugin {
    website.set("https://github.com/gmazzo/gradle-android-test-aggregation-plugin")
    vcsUrl.set("https://github.com/gmazzo/gradle-android-test-aggregation-plugin")

    apiTargets(minGradleVersion, "8.13")

    plugins.create("test-coverage-aggregation") {
        id = "io.github.gmazzo.test.aggregation.coverage"
        displayName = name
        implementationClass =
            "io.github.gmazzo.android.test.aggregation.TestCoverageAggregationPlugin"
        description = "Jacoco coverage aggregation support for Android/JVM modules"
        tags.addAll(
            "android",
            "agp",
            "coverage",
            "jacoco",
            "test",
            "aggregation",
            "jacoco-report-aggregation"
        )
    }

    plugins.create("test-results-aggregation") {
        id = "io.github.gmazzo.test.aggregation.results"
        displayName = name
        implementationClass =
            "io.github.gmazzo.android.test.aggregation.TestResultsAggregationPlugin"
        description = "Test results aggregation support for Android/JVM modules"
        tags.addAll("android", "agp", "test", "aggregation", "test-report-aggregation")
    }
}

fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
    dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

fun DependencyHandler.plugin(dependency: PluginNotationSupplier) =
    plugin(dependency.asProvider())

val oldAGPDependency = dependencies
    .plugin(libs.plugins.android)
    .let { dependencies.create("${it.group}:${it.name}:$minAGPVersion") }

dependencies {
    compileOnly(plugin(libs.plugins.android))
    compileOnly(plugin(libs.plugins.kotlin.multiplatform))

    testFixturesApi(platform(libs.junit.bom))
    testFixturesApi(libs.junit.params)

    testFixturesCompileOnly(gradleKotlinDsl())
    testFixturesCompileOnly(oldAGPDependency)

    "gradle80TestImplementation"(oldAGPDependency)
    "gradle813TestImplementation"(plugin(libs.plugins.android))

    "kotlinTestImplementation"(gradleKotlinDsl())
    "kotlinTestImplementation"(testFixtures(project))
    "kotlinTestImplementation"(plugin(libs.plugins.android))
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

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadataGradle80") {
    pluginClasspath.from(configurations.detachedConfiguration(oldAGPDependency))
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadataGradle813") {
    pluginClasspath.from(configurations.detachedConfiguration(dependencies.plugin(libs.plugins.android)))
}

tasks.withType<JacocoReport>().configureEach {
    reports.xml.required = true
}

tasks.check {
    dependsOn(tasks.withType<JacocoReport>())
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
