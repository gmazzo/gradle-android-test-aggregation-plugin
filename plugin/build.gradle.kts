import com.android.build.api.AndroidPluginVersion
import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.PluginNotationSupplier

plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.gitVersion)
    alias(libs.plugins.gradle.multiapi)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.gradle.testkit.jacoco)
    alias(libs.plugins.publicationsReport)
    `jacoco-report-aggregation`
    `java-test-fixtures`
}

group = "io.github.gmazzo.test.aggregation"
description = "Test Aggregation Plugin for Android"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

val kotlinTest by testing.suites.creating(JvmTestSuite::class)

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

val originUrl = providers
    .exec { commandLine("git", "remote", "get-url", "origin") }
    .standardOutput.asText.map { it.trim() }

gradlePlugin {
    vcsUrl = originUrl
    website = originUrl

    apiTargets(minGradleVersion, "8.13")
    testSourceSets += kotlinTest.sources

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

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    pom {
        name = "${rootProject.name}-${project.name}"
        description = provider { project.description }
        url = originUrl

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit/"
            }
        }

        developers {
            developer {
                id = "gmazzo"
                name = id
                email = "gmazzo65@gmail.com"
            }
        }

        scm {
            connection = originUrl
            developerConnection = originUrl
            url = originUrl
        }
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

val testFixtures by sourceSets

components.named<AdhocComponentWithVariants>("java") {
    sequenceOf(
        testFixtures.apiElementsConfigurationName,
        testFixtures.runtimeElementsConfigurationName,
    ).forEach { withVariantsFromConfiguration(configurations.getByName(it)) { skip() } }
}

tasks.withType<Test>().configureEach {
    testClassesDirs += testFixtures.output.classesDirs
    environment("TEMP_DIR", temporaryDir)
    finalizedBy("${name}CodeCoverageReport")
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

afterEvaluate {
    tasks.named<Jar>("javadocJar") {
        from(tasks.dokkaGeneratePublicationJavadoc)
    }
}

tasks.check {
    dependsOn(tasks.withType<JacocoReport>())
}

tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(tasks.publishPlugins)
}

tasks.publishPlugins {
    enabled = "$version".matches("\\d+(\\.\\d+)+".toRegex())
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
